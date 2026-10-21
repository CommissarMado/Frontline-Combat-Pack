package frontline.combat.fcp.entity.vehicle.Viper;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ViperEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private float barrelRotation = 0f;
    private float barrelRotationOld = 0f;

    public ViperEntity(EntityType<ViperEntity> type, Level world) {
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

}