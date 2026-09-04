package frontline.combat.fcp.entity.vehicle.Huey;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.entity.vehicle.Viper.ViperEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class HueyDoorGunnerM134Entity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private int previousCannonAmmo = -1;
    private float barrelRotation = 0f;
    private float barrelRotationOld = 0f;

    public HueyDoorGunnerM134Entity(EntityType<HueyDoorGunnerM134Entity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // Store previous barrel rotation for smooth interpolation
        barrelRotationOld = barrelRotation;

        // Check if cannon ammo has changed (meaning it was fired)
        int currentAmmo = getAmmoCount("m134");

        // Initialize on first tick
        if (previousCannonAmmo == -1) {
            previousCannonAmmo = currentAmmo;
        }

        // If ammo decreased, increment barrel rotation
        if (currentAmmo < previousCannonAmmo) {
            barrelRotation += 20f; // Increment by 20 degrees per shot
            if (barrelRotation >= 360f) {
                barrelRotation -= 360f; // Wrap around at 360 degrees
            }
        }

        // Update stored ammo count for next tick
        previousCannonAmmo = currentAmmo;
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
}
