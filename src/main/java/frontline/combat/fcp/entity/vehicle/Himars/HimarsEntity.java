package frontline.combat.fcp.entity.vehicle.Himars;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.value.ReloadState;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.entity.vehicle.SteerableVehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * M142 HIMARS. The launcher ("Main") has two guidance modes, picked like ammo types on one shared 6-missile magazine:
 * <ul>
 *   <li>TV (type 0): the gunner aims the launcher like any turret, then flies the missile like a drone
 *       (HimarsMissileEntity, HimarsTvClient); one TV missile at a time.</li>
 *   <li>Coordinates (type 1): SBW's coordinate input screen (right-click). Once coordinates are entered, the launcher
 *       stays locked on the target's bearing at {@link #COORD_ELEVATION} degrees; a shot waits until it is on target.
 *       The missile then lofts to the coordinates.</li>
 * </ul>
 * Both steer as poorly as the Hellfires. With nobody in the gunner seat the launcher returns to rest. Switching mode
 * keeps the loaded missiles. The missiles ("group".."group6") disappear as they're fired.
 */
public class HimarsEntity extends CamoVehicleBase implements SteerableVehicle {

    public static final String MAIN = "Main";
    /** Ammo type index of the coordinate-input mode. */
    public static final int COORDINATE_MODE = 1;
    /** Launcher elevation for coordinate fire, degrees above the horizon. */
    public static final float COORD_ELEVATION = 20f;
    /** The launcher counts as on target within this angle (degrees). */
    private static final double ON_TARGET = 1.5;

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(HimarsEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<BlockPos>> COORD_TARGET = SynchedEntityData.defineId(HimarsEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public int INVENTORY_SIZE = 18;

    private float prevSteeringAngle, wheelRotation, prevWheelRotation;

    /** Coordinate fire: the shot waiting for the launcher to come on target. */
    private @Nullable LivingEntity pendingShooter;
    private boolean shotPending;

    /** Main gun state after the previous tick, to undo SBW's reset on a mode switch. */
    private int lastMode = -1, lastAmmo, lastReloadTime;
    private @Nullable ReloadState lastReloadState;

    /** The TV missile being flown from this launcher, if any: no second TV launch while it is in the air. */
    private @Nullable HimarsMissileEntity tvMissile;

    public HimarsEntity(EntityType<HimarsEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STEERING_ANGLE, 0f);
        this.entityData.define(COORD_TARGET, Optional.empty());
    }

    /** True while the gunner has the coordinate-input mode selected. */
    public boolean coordinateMode() {
        GunData data = getGunData(MAIN);
        return data != null && data.selectedAmmoType.get() == COORDINATE_MODE;
    }

    /** Missiles left in the launcher (6 = full). */
    public int missilesLoaded() {
        return getAmmoCount(MAIN);
    }

    // ---- coordinate fire: lock on the target, then launch ----

    /** The gunner's entered coordinates (from their client, HimarsTargetPacket), or the last fired-at target. */
    public void setCoordTarget(@Nullable BlockPos pos) {
        this.entityData.set(COORD_TARGET, Optional.ofNullable(pos));
    }

    private @Nullable Vec3 coordTarget() {
        return this.entityData.get(COORD_TARGET).map(BlockPos::getCenter).orElse(null);
    }

    private boolean gunnerSeated() {
        return getNthEntity(getTurretControllerIndex()) != null;
    }

    public void setTvMissile(@Nullable HimarsMissileEntity missile) {
        this.tvMissile = missile;
    }

    /** The TV missile {@code pilot} is flying from this launcher, or null. */
    public @Nullable HimarsMissileEntity tvMissileOf(Entity pilot) {
        return flyingTvMissile() && tvMissile.pilotId() == pilot.getId() && tvMissile.level() == level() ? tvMissile : null;
    }

    private boolean flyingTvMissile() {
        if (tvMissile != null && (!tvMissile.isAlive() || tvMissile.pilotId() < 0)) tvMissile = null;
        return tvMissile != null;
    }

    @Override
    public void vehicleShoot(@Nullable LivingEntity living, @Nullable UUID uuid, @Nullable Vec3 targetPos) {
        if (!this.level().isClientSide() && !coordinateMode() && flyingTvMissile()) return;
        if (this.level().isClientSide() || isWreck() || !coordinateMode()) {
            super.vehicleShoot(living, uuid, targetPos);
            return;
        }
        if (targetPos == null) {
            if (living instanceof Player player) {
                player.displayClientMessage(Component.translatable("tips.fcp.himars.no_coordinates"), true);
            }
            return;
        }
        GunData data = getGunData(MAIN);
        if (data == null || !data.canShoot(getAmmoSupplier())) return;
        setCoordTarget(BlockPos.containing(targetPos));
        pendingShooter = living;
        shotPending = true;
    }

    /** Launch direction for coordinate fire: the target's bearing, {@link #COORD_ELEVATION} degrees up. */
    private @Nullable Vec3 layDirection() {
        Vec3 target = coordTarget();
        if (target == null) return null;
        Vec3 to = position().vectorTo(target);
        double flat = Math.sqrt(to.x * to.x + to.z * to.z);
        if (flat < 1.0e-3) return null;
        double up = Math.tan(Math.toRadians(COORD_ELEVATION)) * flat;
        return new Vec3(to.x, up, to.z).normalize();
    }

    /** In coordinate mode with coordinates entered, the launcher stays laid on them; the gunner's view doesn't move it. */
    private @Nullable Vec3 lockedLay() {
        return coordinateMode() && !isWreck() ? layDirection() : null;
    }

    @Override
    public void adjustTurretAngle() {
        Vec3 lay = lockedLay();
        if (lay == null) {
            super.adjustTurretAngle();
            return;
        }
        turretAutoAimFromVector(lay);
    }

    /** No gunner: the launcher swings back to its rest position (straight ahead, level) at its normal turn speed. */
    private void stowLauncher() {
        if (isWreck()) return;
        setTurretYRot(Mth.approachDegrees(getTurretYRot(), 0f, getTurretTurnYSpeed()));
        setTurretXRot(Mth.approach(getTurretXRot(), 0f, getTurretTurnXSpeed()));
    }

    private void tickCoordinateFire() {
        if (!shotPending || this.level().isClientSide()) return;
        Vec3 lay = lockedLay();
        if (lay == null || !gunnerSeated()) {
            shotPending = false;
            return;
        }
        double off = Math.toDegrees(Math.acos(Mth.clamp(getBarrelVector(1f).normalize().dot(lay), -1, 1)));
        if (off <= ON_TARGET) {
            shotPending = false;
            super.vehicleShoot(pendingShooter, (UUID) null, coordTarget());
        }
    }

    /**
     * Switching guidance mode is only a change of ammo type on one shared magazine, but SBW's switch resets the
     * gun (and its reload). Put the loaded missiles and any reload in progress back as they were.
     */
    private void keepLoadOnModeSwitch() {
        if (this.level().isClientSide()) return;
        GunData data = getGunData(MAIN);
        if (data == null || lastMode < 0 || data.selectedAmmoType.get() == lastMode) return;
        int ammo = lastAmmo, reloadTime = lastReloadTime;
        ReloadState reloadState = lastReloadState;
        modifyGunData(MAIN, d -> {
            d.ammo.set(ammo);
            if (reloadTime > 0 && reloadState != null) {
                d.reload.setState(reloadState);
                d.reload.setTime(reloadTime);
            }
        });
    }

    private void rememberLoad() {
        if (this.level().isClientSide()) return;
        GunData data = getGunData(MAIN);
        if (data == null) return;
        lastMode = data.selectedAmmoType.get();
        lastAmmo = data.ammo.get();
        lastReloadTime = data.reload.time();
        lastReloadState = data.reload.state();
    }

    // ---- steering (as the other FCP trucks) ----

    public float getSteeringAngle() {
        return this.entityData.get(STEERING_ANGLE);
    }

    public void setSteeringAngle(float angle) {
        this.entityData.set(STEERING_ANGLE, angle);
    }

    public float getPrevSteeringAngle() {
        return prevSteeringAngle;
    }

    public float getWheelRotation() {
        return wheelRotation;
    }

    public float getPrevWheelRotation() {
        return prevWheelRotation;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SteeringAngle", this.getSteeringAngle());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SteeringAngle")) setSteeringAngle(compound.getFloat("SteeringAngle"));
    }

    /** Client: set by HimarsTvClient while the local player's view is out at a TV missile. */
    public static boolean localTvViewAway;

    /**
     * Client: SBW runs gravity and collision on every client's copy of a vehicle, only easing it back towards the
     * server's position. While this client's view is out at a TV missile (HimarsTvView), or the ground around the
     * HIMARS hasn't all arrived again, that would drop it through missing chunks - and below the world the client
     * deletes it for good. So it holds still until the ground is back (unless this client is driving it).
     */
    @Override
    public void tick() {
        boolean hold = this.level().isClientSide() && !isControlledByLocalInstance() && (localViewAway() || !groundLoaded());
        debugLog(hold);
        if (hold) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.tick();
        keepToServerHeight();
    }

    /**
     * Client: a HIMARS someone else drives (or nobody) is wherever the server says. After a TV flight SBW's client
     * physics let it fall straight through loaded ground while the server's copy sat still (and a parked vehicle sends
     * no position updates to correct it), so it may never sink below the server's position. That position is the
     * network codec's base: only real spawn and move packets set it (SBW's client code also feeds lerpTo, so that
     * follows the fall).
     */
    private void keepToServerHeight() {
        if (!this.level().isClientSide() || isControlledByLocalInstance()) return;
        double serverY = getPositionCodec().decode(0, 0, 0).y;
        if (getY() < serverY - 0.05) {
            setPos(getX(), serverY, getZ());
            setDeltaMovement(getDeltaMovement().multiply(1, 0, 1));
        }
    }

    // TEMPORARY diagnostics for the TV-return sinking bug: remove once found.
    private static final org.slf4j.Logger DBG = com.mojang.logging.LogUtils.getLogger();
    private int dbgUntil;

    private void debugLog(boolean hold) {
        boolean flying = flyingTvMissile() || (level().isClientSide() && localTvViewAway);
        if (flying) dbgUntil = tickCount + 200;
        if (tickCount > dbgUntil || tickCount % 5 != 0) return;
        String passengers = getPassengers().stream()
                .map(p -> String.format("%s y=%.2f", p.getName().getString(), p.getY())).toList().toString();
        DBG.info("[HIMARS-DBG] {} t={} id={} y={} dy={} hold={} viewAway={} ground={} chunk={} passengers={}",
                level().isClientSide() ? "CLIENT" : "SERVER", tickCount, getId(), String.format("%.2f", getY()),
                String.format("%.3f", getDeltaMovement().y), hold, localTvViewAway, groundLoaded(),
                level().hasChunkAt(blockPosition()), passengers);
    }

    private boolean localViewAway() {
        return localTvViewAway && getPassengers().stream().anyMatch(p -> p instanceof Player player && player.isLocalPlayer());
    }

    @SuppressWarnings("deprecation")
    private boolean groundLoaded() {
        var box = getBoundingBox().inflate(16);
        return this.level().hasChunksAt(Mth.floor(box.minX), Mth.floor(box.minZ), Mth.floor(box.maxX), Mth.floor(box.maxZ));
    }

    @Override
    public void baseTick() {
        keepLoadOnModeSwitch();
        super.baseTick();
        if (!gunnerSeated()) stowLauncher();
        tickCoordinateFire();
        rememberLoad();

        prevSteeringAngle = getSteeringAngle();
        float angle = getSteeringAngle();
        double speed = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
        boolean moving = speed > 0.05;
        if (leftInputDown() && !rightInputDown()) {
            angle = Math.min(35f, angle + 2f);
            setSteeringAngle(angle);
        } else if (rightInputDown() && !leftInputDown()) {
            angle = Math.max(-35f, angle - 2f);
            setSteeringAngle(angle);
        } else if (moving && Math.abs(angle) > 0.5f) {
            angle *= 0.9f;
            setSteeringAngle(angle);
        }
        if (moving && Math.abs(angle) > 1f) setYRot(getYRot() + angle * 0.009f * (float) speed);
        prevWheelRotation = wheelRotation;
        wheelRotation += (float) (speed * 20f);
    }
}
