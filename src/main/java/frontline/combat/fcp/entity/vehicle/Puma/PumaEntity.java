package frontline.combat.fcp.entity.vehicle.Puma;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;

/**
 * Puma IFV, both variants (puma, puma_atgm) on one class; the registry name picks the geo and skins.
 * <p>
 * The ATGM variant's Spike launcher sits on the side of the turret with its own elevation pivot
 * (the "ATGM" bone), not on the gun. SBW has no slot for that, so it is exposed to the vehicle JSON as
 * the transform "AtgmBarrel" - built exactly like SBW's barrel transform (turret -> pivot -> gun pitch)
 * but at the launcher's pivot - which the ATGM's ShootPos uses, so missiles leave the launcher tubes.
 */
public class PumaEntity extends CamoVehicleBase {

    public static final String ATGM_WEAPON = "ATGM";
    public static final String ATGM_BARREL = "AtgmBarrel";

    /** The "ATGM" bone's pivot relative to the turret pivot, in blocks (config convention). */
    private static final double[] ATGM_PIVOT = {0.9470, 0.3184, 0.2472};

    public int INVENTORY_SIZE = 9;

    public PumaEntity(EntityType<PumaEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    /** Same as SBW's getBarrelTransform, but pivoting at the launcher. */
    private Matrix4d atgmBarrelTransform(float partialTick) {
        Matrix4d t = getTurretTransform(partialTick);
        t.translate(ATGM_PIVOT[0], ATGM_PIVOT[1], ATGM_PIVOT[2]);
        t.rotate(Axis.XP.rotationDegrees(Mth.lerp(partialTick, getTurretXRotO(), getTurretXRot())));
        return t;
    }

    @Override
    public Matrix4d getTransformFromString(@Nullable String string, float ticks) {
        return ATGM_BARREL.equals(string) ? atgmBarrelTransform(ticks) : super.getTransformFromString(string, ticks);
    }

    /** True once the weapon has {@code count} or fewer rounds left (used to hide fired missiles). */
    public boolean GetWeaponState(String WeaponName, int Count) {
        return getAmmoCount(WeaponName) <= Count;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
}
