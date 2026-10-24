package frontline.combat.fcp.entity.vehicle.Khrizantema;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Vector4d;

import java.util.UUID;

/**
 * 9P157-2 Khrizantema-S: BMP-3 hull with a 9M123 launcher arm that stows inside the hull.
 * <p>
 * While someone is in the ATGM gunner seat the launcher deploys. Its three animations start together,
 * on two synced progress values (in ticks):
 * <ul>
 *   <li>SIGHT (0..MAST): the sight doors ("DOR3"/"DOR4") swing open over the first DOORS ticks while
 *       the mast ("turret2") rises and its hatch ("hatch1") opens - after this the mast turns with
 *       the gunner's look;</li>
 *   <li>ARM (0..LAUNCHER, the Bradley TOW "up" cue): the roof hatch ("hatch7") opens and, from halfway
 *       through it, the arm ("turret") rises and swings up while the launcher ("barrel") unfolds -
 *       after this the turret is free to traverse and fire.</li>
 * </ul>
 * Reloading brings the turret back to zero, folds the arm back in (ARM in reverse, at the TOW
 * "down" cue's length) and raises it again once reloaded; the sight stays up. Leaving the seat stows
 * both. "Locked" means the turret (or mast) is driven back to zero at its traverse speed instead of
 * following the gunner, and its part doesn't fold until it is there. The gunner can't fire until the
 * arm is fully up and unlocked.
 * <p>
 * The gunner looks through the sight behind the doors ("turret3"/"barrel3"), which follows the
 * gunner's look on its own (so it keeps working while the launcher is folded for a reload):
 * "SightBarrel" carries the gunner camera.
 * <p>
 * The bow PKT ("pkt") is aimed by its own seat with the same math as SBW's BoundBones (see
 * Uh60MinigunEntity): "PktBarrel" (yaw + pitch about the gun's pivot) carries its ShootPos,
 * "PktTurret" (yaw only) its camera. Its shots fly along the seat's aim ("Passenger" direction), which
 * is also what the angles are measured from.
 */
public class KhrizantemaEntity extends CamoVehicleBase {

    public static final String ATGM_WEAPON = "ATGM";
    public static final int GUNNER_SEAT = 1, PKT_SEAT = 2;

    /** Animation lengths, in ticks. LAUNCHER = Squad's bradley_TOW_up cue (6.30 s). */
    public static final float DOORS = 15, MAST = 30, LAUNCHER = 126;
    /** Within LAUNCHER: the roof hatch takes this long, and the arm starts halfway through it. */
    public static final float HATCH = 50;
    /** The arm folds back in over the bradley_TOW_down cue (5.66 s). */
    private static final float LAUNCHER_STOW_STEP = LAUNCHER / 113.2f;

    /** The mast's traverse speed (deg/tick) and pitch limits. */
    private static final float MAST_SPEED = 6f, MAST_MIN_PITCH = -10f, MAST_MAX_PITCH = 20f;
    private static final float AT_REST = 0.5f;
    /** Traverse limit each side for the launcher, sight and mast, in degrees. */
    private static final float TRAVERSE = 85f;

    /** Bone pivots, in blocks (config convention): "pkt", "turret3", and "barrel3" relative to it. */
    private static final Vec3 PKT_PIVOT = new Vec3(1.3060, 1.7096, 2.2478);
    private static final Vec3 SIGHT_PIVOT = new Vec3(-0.9045, 2.0107, 2.8044);
    private static final Vec3 SIGHT_BARREL = new Vec3(0, -0.06375, 0);
    /** How far the launcher hitbox drops (blocks) with the arm folded, to sit inside the hull. */
    private static final double LAUNCHER_HITBOX_SINK = 1.6;

    private static final EntityDataAccessor<Float> SIGHT =
            SynchedEntityData.defineId(KhrizantemaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ARM =
            SynchedEntityData.defineId(KhrizantemaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SIGHT_WANTED =
            SynchedEntityData.defineId(KhrizantemaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ARM_WANTED =
            SynchedEntityData.defineId(KhrizantemaEntity.class, EntityDataSerializers.BOOLEAN);

    private float sightO, sightNow, armO, armNow;
    private float mastYaw, mastPitch, mastYawO, mastPitchO;
    private float sightYaw, sightPitch, sightYawO, sightPitchO;

    public int INVENTORY_SIZE = 9;

    public KhrizantemaEntity(EntityType<KhrizantemaEntity> type, Level world) {
        super(type, world);
        getPositionTransform().put("PktBarrel", (Float pt) -> pktTransform(pt, true));
        getVectorTransform().put("PktBarrel", (Float pt) -> forward(pktTransform(pt, true)));
        getPositionTransform().put("PktTurret", (Float pt) -> pktTransform(pt, false));
        getPositionTransform().put("SightBarrel", this::sightTransform);
        getVectorTransform().put("SightBarrel", (Float pt) -> forward(sightTransform(pt)));
        getPositionTransform().put("LauncherHitbox", this::launcherHitboxTransform);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SIGHT, 0f);
        this.entityData.define(ARM, 0f);
        this.entityData.define(SIGHT_WANTED, false);
        this.entityData.define(ARM_WANTED, false);
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    // ---- deploy progress -------------------------------------------------------------------

    public float sight() {
        return this.entityData.get(SIGHT);
    }

    public float arm() {
        return this.entityData.get(ARM);
    }

    /** Sight progress smoothed over the partial tick, for rendering. */
    public float sight(float partialTick) {
        return Mth.lerp(partialTick, sightO, sightNow);
    }

    /** Arm progress smoothed over the partial tick, for rendering. */
    public float arm(float partialTick) {
        return Mth.lerp(partialTick, armO, armNow);
    }

    /** True while the turret must sit at zero: arm not fully up, or about to fold. */
    public boolean turretLocked() {
        return arm() < LAUNCHER || !this.entityData.get(ARM_WANTED);
    }

    private boolean mastLocked() {
        return sight() < MAST || !this.entityData.get(SIGHT_WANTED);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        sightO = sightNow;
        sightNow = sight();
        armO = armNow;
        armNow = arm();

        updateMast();
        if (turretLocked()) lockTurret();
        if (!level().isClientSide) stepDeploy();
    }

    private void stepDeploy() {
        Entity gunner = getNthEntity(GUNNER_SEAT);
        GunData atgm = gunner == null ? null : getGunData(GUNNER_SEAT);
        boolean sightWanted = gunner != null && !isWreck();
        boolean armWanted = sightWanted && !(atgm != null && atgm.reloading());
        this.entityData.set(SIGHT_WANTED, sightWanted);
        this.entityData.set(ARM_WANTED, armWanted);

        float a = arm();
        if (armWanted) {
            a = Math.min(LAUNCHER, a + 1);
        } else if (a > 0 && (a < LAUNCHER || turretAtRest())) {
            // Turret home first, then fold.
            a = Math.max(0, a - LAUNCHER_STOW_STEP);
        }
        this.entityData.set(ARM, a);

        float s = sight();
        if (sightWanted) {
            s = Math.min(MAST, s + 1);
        } else if (s > 0 && (s < MAST || mastAtRest())) {
            // Mast home first, then lower.
            s = Math.max(0, s - 1);
        }
        this.entityData.set(SIGHT, s);
    }

    // ---- turret lock -----------------------------------------------------------------------

    /** Locked: don't follow the gunner (lockTurret drives it to zero instead). */
    @Override
    public void adjustTurretAngle() {
        if (!turretLocked()) super.adjustTurretAngle();
    }

    private void lockTurret() {
        float ys = getTurretTurnYSpeed(), xs = getTurretTurnXSpeed();
        float y = Mth.wrapDegrees(getTurretYRot());
        setTurretYRot(getTurretYRot() - Mth.clamp(y, -ys, ys));
        setTurretXRot(getTurretXRot() - Mth.clamp(getTurretXRot(), -xs, xs));
        setTurretYRotLock(0f);
    }

    private boolean turretAtRest() {
        return Math.abs(Mth.wrapDegrees(getTurretYRot())) < AT_REST && Math.abs(getTurretXRot()) < AT_REST;
    }

    /** No firing until the launcher is up and the turret is free. */
    @Override
    public boolean canShoot(@Nullable LivingEntity living) {
        if (living != null && getSeatIndex(living) == GUNNER_SEAT && turretLocked()) return false;
        return super.canShoot(living);
    }

    @Override
    public void vehicleShoot(@Nullable LivingEntity living, @Nullable UUID uuid, @Nullable Vec3 targetPos) {
        if (living != null && getSeatIndex(living) == GUNNER_SEAT && turretLocked()) return;
        super.vehicleShoot(living, uuid, targetPos);
    }

    // ---- sight mast ------------------------------------------------------------------------

    /**
     * The mast and the gunner's sight follow the gunner's look relative to the hull, each at its own
     * speed: the mast once it is up, the sight once its doors are open (also while the launcher is
     * folded for a reload). Otherwise they return to zero.
     */
    private void updateMast() {
        mastYawO = mastYaw;
        mastPitchO = mastPitch;
        sightYawO = sightYaw;
        sightPitchO = sightPitch;
        float lookYaw = 0, lookPitch = 0;
        Entity gunner = getNthEntity(GUNNER_SEAT);
        if (gunner != null) {
            Vec3 look = gunner.getViewVector(1f);
            Vec3 hull = hullForward();
            // Same traverse limit as the launcher ("TurretYawRange") and the gunner seat.
            lookYaw = Mth.clamp((float) -Mth.wrapDegrees(-VehicleVecUtils.getYRotFromVector(look) + VehicleVecUtils.getYRotFromVector(hull)),
                    -TRAVERSE, TRAVERSE);
            lookPitch = (float) -Mth.wrapDegrees(-VehicleVecUtils.getXRotFromVector(look) + VehicleVecUtils.getXRotFromVector(hull));
        }
        boolean mastFree = !mastLocked() && gunner != null;
        float wantYaw = mastFree ? lookYaw : 0, wantPitch = mastFree ? Mth.clamp(lookPitch, MAST_MIN_PITCH, MAST_MAX_PITCH) : 0;
        mastYaw += Mth.clamp(Mth.wrapDegrees(wantYaw - mastYaw), -MAST_SPEED, MAST_SPEED);
        mastPitch += Mth.clamp(wantPitch - mastPitch, -MAST_SPEED, MAST_SPEED);

        boolean sightFree = gunner != null && this.entityData.get(SIGHT_WANTED) && sight() >= DOORS;
        float ys = getTurretTurnYSpeed(), xs = getTurretTurnXSpeed();
        wantYaw = sightFree ? lookYaw : 0;
        wantPitch = sightFree ? Mth.clamp(lookPitch, -getTurretMaxPitch(), -getTurretMinPitch()) : 0;
        sightYaw += Mth.clamp(Mth.wrapDegrees(wantYaw - sightYaw), -ys, ys);
        sightPitch += Mth.clamp(wantPitch - sightPitch, -xs, xs);
    }

    public float sightYaw(float partialTick) {
        return sightYawO + Mth.wrapDegrees(sightYaw - sightYawO) * partialTick;
    }

    public float sightPitch(float partialTick) {
        return Mth.lerp(partialTick, sightPitchO, sightPitch);
    }

    private boolean mastAtRest() {
        return Math.abs(Mth.wrapDegrees(mastYaw)) < AT_REST && Math.abs(mastPitch) < AT_REST;
    }

    public float mastYaw(float partialTick) {
        return mastYawO + Mth.wrapDegrees(mastYaw - mastYawO) * partialTick;
    }

    public float mastPitch(float partialTick) {
        return Mth.lerp(partialTick, mastPitchO, mastPitch);
    }

    private Vec3 hullForward() {
        Matrix4d t = getVehicleTransform(1f);
        Vector4d o = t.transform(new Vector4d(0, 0, 0, 1)), f = t.transform(new Vector4d(0, 0, 1, 1));
        return new Vec3(f.x - o.x, f.y - o.y, f.z - o.z);
    }

    // ---- bow PKT (BoundBones math, by hand) ---------------------------------------------------

    /** Yaw (degrees) from the PKT's rest direction to its gunner's aim. */
    public float pktYaw(float partialTicks) {
        if (getNthEntity(PKT_SEAT) == null) return 0f;
        Vec3 target = getShootVec(PKT_SEAT, partialTicks), rest = getDefaultBarrelDirection(PKT_SEAT, partialTicks);
        if (target == null || rest == null) return 0f;
        return (float) Mth.wrapDegrees(-VehicleVecUtils.getYRotFromVector(target) + VehicleVecUtils.getYRotFromVector(rest)) * -1f;
    }

    /** Pitch (degrees) from the PKT's rest direction to its gunner's aim. */
    public float pktPitch(float partialTicks) {
        if (getNthEntity(PKT_SEAT) == null) return 0f;
        Vec3 target = getShootVec(PKT_SEAT, partialTicks), rest = getDefaultBarrelDirection(PKT_SEAT, partialTicks);
        if (target == null || rest == null) return 0f;
        return (float) Mth.wrapDegrees(-VehicleVecUtils.getXRotFromVector(target) + VehicleVecUtils.getXRotFromVector(rest)) * -1f;
    }

    /** The gun pivots on one bone, so yaw and pitch both turn about its pivot. */
    private Matrix4d pktTransform(float partialTicks, boolean withPitch) {
        Matrix4d t = getVehicleTransform(partialTicks);
        t.translate(PKT_PIVOT.x, PKT_PIVOT.y, PKT_PIVOT.z);
        t.rotate(Axis.YP.rotationDegrees(pktYaw(partialTicks)));
        if (withPitch) t.rotate(Axis.XP.rotationDegrees(-pktPitch(partialTicks)));
        return t;
    }

    // ---- gunner sight ------------------------------------------------------------------------

    /** The sight ("turret3" / "barrel3"): yaw about its pivot, then pitch (bone angles, BoundBones signs). */
    private Matrix4d sightTransform(float partialTicks) {
        Matrix4d t = getVehicleTransform(partialTicks);
        t.translate(SIGHT_PIVOT.x, SIGHT_PIVOT.y, SIGHT_PIVOT.z);
        t.rotate(Axis.YP.rotationDegrees(sightYaw(partialTicks)));
        t.translate(SIGHT_BARREL.x, SIGHT_BARREL.y, SIGHT_BARREL.z);
        t.rotate(Axis.XP.rotationDegrees(-sightPitch(partialTicks)));
        return t;
    }

    /** The launcher's hitbox rides the turret, sunk into the hull as the arm folds away. */
    private Matrix4d launcherHitboxTransform(float partialTicks) {
        Matrix4d t = getTurretTransform(partialTicks);
        t.translate(0, -LAUNCHER_HITBOX_SINK * (1 - arm() / LAUNCHER), 0);
        return t;
    }

    /** The transform's forward (+Z) direction in the world. */
    private static Vec3 forward(Matrix4d t) {
        Vector4d o = t.transform(new Vector4d(0, 0, 0, 1)), f = t.transform(new Vector4d(0, 0, 1, 1));
        return new Vec3(o.x, o.y, o.z).vectorTo(new Vec3(f.x, f.y, f.z));
    }

    // ---- missiles ----------------------------------------------------------------------------

    /** True once the weapon has {@code count} or fewer rounds left (used to hide fired missiles). */
    public boolean GetWeaponState(String weaponName, int count) {
        return getAmmoCount(weaponName) <= count;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
}
