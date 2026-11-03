package frontline.combat.fcp.entity.vehicle.Ka52;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector4d;

/**
 * Ka-52 Alligator. The pilot flies with the S-8 rockets, 4 lock-on 9M120s and 2 Iglas; the co-pilot (turret controller)
 * aims the nose camera turret, which the 2A42 on the right side ("barrel2") and the 6 wire-guided 9M120s follow.
 * <p>
 * The cannon isn't on the camera turret, so its shots get their own transform, "CannonBarrel": the camera turret's yaw
 * and pitch, clamped to the real mount's limits ({@link #CANNON_MIN_YAW}..{@link #CANNON_MAX_YAW},
 * {@link #CANNON_MAX_ELEVATION} up to {@link #CANNON_MAX_DEPRESSION} down), about the cannon's own pivot (the same
 * rotations SBW's turret/barrel transforms use). The camera overlay's aim line (FcpPilotOverlay) shows where it points.
 * <p>
 * The coaxial rotors are drawn by Ka52Model; the landing gear retracts once it's more than {@link #GEAR_HEIGHT} blocks
 * clear of the ground and comes down again below that (client-side, visual only).
 */
public class Ka52Entity extends CamoVehicleBase {

    public static final String CANNON = "Cannon";
    /** "barrel2" pivot and the muzzle relative to it (config space, blocks; Ka52Gen, 0.85x geo). */
    private static final Vec3 CANNON_PIVOT = new Vec3(-0.579, 1.0, -0.2879), CANNON_MUZZLE = new Vec3(-0.2653, -0.0689, 2.7435);
    /**
     * The 2A42 mount's travel, in SBW turret angles (yaw + = port, pitch + = down): 30 deg out to starboard, 2 deg in to
     * port, +3.5 deg elevation, -37 deg depression.
     */
    public static final float CANNON_MIN_YAW = -30, CANNON_MAX_YAW = 2, CANNON_MAX_ELEVATION = 3.5f, CANNON_MAX_DEPRESSION = 37;
    /** Gear travel time (ticks) and the height (blocks) it retracts above. */
    private static final float GEAR_TICKS = 40;
    private static final double GEAR_HEIGHT = 6;

    public int INVENTORY_SIZE = 27;

    /** Client: gear position, 0 = down, 1 = up. */
    private float gear, gearO;

    public Ka52Entity(EntityType<Ka52Entity> type, Level world) {
        super(type, world);
        getPositionTransform().put("CannonBarrel", this::cannonTransform);
        getVectorTransform().put("CannonBarrel", (Float pt) -> forward(cannonTransform(pt)));
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    /** The Igla only flies when locked on: no launch without a target. */
    @Override
    protected java.util.Set<String> lockOnlyWeapons() {
        return java.util.Set.of("Igla");
    }

    @Override
    public void vehicleShoot(@Nullable net.minecraft.world.entity.LivingEntity living, @Nullable java.util.UUID target, @Nullable net.minecraft.world.phys.Vec3 targetPos) {
        if (refuseUnlocked(living, target)) return;
        super.vehicleShoot(living, target, targetPos);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    /** The cannon's yaw (SBW turret convention): the camera turret's, clamped to the mount. */
    public float cannonYaw(float partialTicks) {
        return Mth.clamp(Mth.lerp(partialTicks, getTurretYRotO(), getTurretYRot()), CANNON_MIN_YAW, CANNON_MAX_YAW);
    }

    /** The cannon's pitch (SBW turret convention, + = down): the camera turret's, clamped to the mount. */
    public float cannonPitch(float partialTicks) {
        return Mth.clamp(Mth.lerp(partialTicks, getTurretXRotO(), getTurretXRot()), -CANNON_MAX_ELEVATION, CANNON_MAX_DEPRESSION);
    }

    /** The 2A42: its clamped yaw, then pitch, about the cannon's pivot. */
    private Matrix4d cannonTransform(float partialTicks) {
        Matrix4d t = getVehicleTransformWithCustomPitch(partialTicks);
        t.translate(CANNON_PIVOT.x, CANNON_PIVOT.y, CANNON_PIVOT.z);
        t.rotate(Axis.YP.rotationDegrees(cannonYaw(partialTicks)));
        t.rotate(Axis.XP.rotationDegrees(cannonPitch(partialTicks)));
        return t;
    }

    /** The cannon's muzzle in the world. */
    public Vec3 cannonMuzzle(float partialTicks) {
        Vector4d m = cannonTransform(partialTicks).transform(new Vector4d(CANNON_MUZZLE.x, CANNON_MUZZLE.y, CANNON_MUZZLE.z, 1));
        return new Vec3(m.x, m.y, m.z);
    }

    /** The cannon's line of fire in the world. */
    public Vec3 cannonDirection(float partialTicks) {
        return forward(cannonTransform(partialTicks)).normalize();
    }

    /** The transform's forward (+Z) direction in the world. */
    private static Vec3 forward(Matrix4d t) {
        Vector4d o = t.transform(new Vector4d(0, 0, 0, 1)), f = t.transform(new Vector4d(0, 0, 1, 1));
        return new Vec3(o.x, o.y, o.z).vectorTo(new Vec3(f.x, f.y, f.z));
    }

    /** True once {@code weapon} has {@code count} or fewer rounds left (hides fired missiles). */
    public boolean fired(String weapon, int count) {
        return getAmmoCount(weapon) <= count;
    }

    /** Gear retraction smoothed over the partial tick and eased, 0 = down, 1 = up. */
    public float gear(float partialTick) {
        float p = Mth.lerp(partialTick, gearO, gear);
        return p * p * (3 - 2 * p);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (level().isClientSide()) {
            gearO = gear;
            gear = Mth.approach(gear, airborne() ? 1 : 0, 1 / GEAR_TICKS);
        }
    }

    /** More than GEAR_HEIGHT blocks of clear air below. */
    private boolean airborne() {
        if (onGround()) return false;
        Vec3 from = position(), to = from.subtract(0, GEAR_HEIGHT, 0);
        return level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this)).getType() == HitResult.Type.MISS;
    }
}
