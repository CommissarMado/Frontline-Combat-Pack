package frontline.combat.fcp.entity.vehicle.Viper;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Vector4d;
import net.minecraft.world.phys.Vec3;

/**
 * AH-1Z Viper. The co-pilot/gunner (turret controller) aims the nose sight ("camera"): SBW's turret and barrel are the
 * sight, pivoting about the camera bone's pivot (TurretPos, BarrelPos 0), and the gunner's view sits just in front of its
 * cube. The M197 under the chin follows the sight like the Ka-52's 2A42 follows its camera: same yaw and pitch, about the
 * gun's own pivots, through the custom transforms "GunTurret" (its hitbox) and "GunBarrel" (its shots), clamped to the gun's
 * own travel ({@link #GUN_MAX_YAW} each side, {@link #GUN_MAX_ELEVATION} up to {@link #GUN_MAX_DEPRESSION} down). The
 * sight itself sees further: 90 deg each side and 5 deg up (seat + TurretYawRange / TurretPitchRange in the config).
 */
public class ViperEntity extends CamoVehicleBase {

    /** The M197's "turret" pivot and its "barrel" pivot relative to it (config space, blocks; viper geo). */
    private static final Vec3 GUN_PIVOT = new Vec3(0.0, 0.8125, 3.237137), GUN_BARREL = new Vec3(0.0, 0.0, 0.069288);
    /** The M197's travel, in SBW turret angles (pitch + = down): 75 deg each side, level up to 60 deg down. */
    public static final float GUN_MAX_YAW = 75, GUN_MAX_ELEVATION = 0, GUN_MAX_DEPRESSION = 60;

    public int INVENTORY_SIZE = 27;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private float barrelRotation = 0f;
    private float barrelRotationOld = 0f;

    public ViperEntity(EntityType<ViperEntity> type, Level world) {
        super(type, world);
        getPositionTransform().put("GunTurret", this::gunTurretTransform);
        getVectorTransform().put("GunTurret", (Float pt) -> forward(gunTurretTransform(pt)));
        getPositionTransform().put("GunBarrel", this::gunBarrelTransform);
        getVectorTransform().put("GunBarrel", (Float pt) -> forward(gunBarrelTransform(pt)));
    }

    /** The Sidewinder only flies when locked on: no launch without a target. */
    @Override
    protected java.util.Set<String> lockOnlyWeapons() {
        return java.util.Set.of("Sidewinder");
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

    @Override
    public void baseTick() {
        super.baseTick();

        // Same spin system as the UH-60 miniguns (see Uh60MinigunEntity#baseTick): the barrels
        // turn continuously while SBW reports the cannon's shot timer as active, instead of
        // stepping once per round that left the ammo count - at the Squad 2A42 fire rate the
        // per-shot steps no longer read as a spinning gatling. The angle grows unbounded (no
        // wrap) so the interpolated render never sweeps backwards; the model converts to radians.
        barrelRotationOld = barrelRotation;
        if (isWeaponFiring("Cannon")) {
            barrelRotation += 80f;
        }
    }

    public boolean GetWeaponState(String WeaponName, int Count) {
        if (getAmmoCount(WeaponName) == Count)
            return true;
        else if (getAmmoCount(WeaponName) < Count)
            return true;
        else
            return false;

    }

    public float getBarrelRot() {
        return barrelRotation;
    }

    public float getBarrelRot0() {
        return barrelRotationOld;
    }


    /** The gun's yaw (SBW turret convention): the sight's, clamped to the mount. */
    public float gunYaw(float partialTicks) {
        return Mth.clamp(Mth.lerp(partialTicks, getTurretYRotO(), getTurretYRot()), -GUN_MAX_YAW, GUN_MAX_YAW);
    }

    /** The gun's pitch (SBW turret convention, + = down): the sight's, clamped to the mount. */
    public float gunPitch(float partialTicks) {
        return Mth.clamp(Mth.lerp(partialTicks, getTurretXRotO(), getTurretXRot()), -GUN_MAX_ELEVATION, GUN_MAX_DEPRESSION);
    }

    /** The M197's mount: the sight's yaw about the gun's turret pivot. */
    private Matrix4d gunTurretTransform(float partialTicks) {
        Matrix4d t = getVehicleTransformWithCustomPitch(partialTicks);
        t.translate(GUN_PIVOT.x, GUN_PIVOT.y, GUN_PIVOT.z);
        t.rotate(Axis.YP.rotationDegrees(gunYaw(partialTicks)));
        return t;
    }

    /** The M197's barrel: the mount, then the sight's pitch about the gun's barrel pivot. */
    private Matrix4d gunBarrelTransform(float partialTicks) {
        Matrix4d t = gunTurretTransform(partialTicks);
        t.translate(GUN_BARREL.x, GUN_BARREL.y, GUN_BARREL.z);
        t.rotate(Axis.XP.rotationDegrees(gunPitch(partialTicks)));
        return t;
    }

    /** The transform's forward (+Z) direction in the world. */
    private static Vec3 forward(Matrix4d t) {
        Vector4d o = t.transform(new Vector4d(0, 0, 0, 1)), f = t.transform(new Vector4d(0, 0, 1, 1));
        return new Vec3(o.x, o.y, o.z).vectorTo(new Vec3(f.x, f.y, f.z));
    }
}
