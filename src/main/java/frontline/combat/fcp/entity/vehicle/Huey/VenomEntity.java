package frontline.combat.fcp.entity.vehicle.Huey;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class VenomEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    // The front sensor now has real "turret"/"barrel" bones in venom.geo.json (previously it had
    // none, so the old hand-rolled camera-tracking code here was inert dead weight). No custom
    // Java is needed for it, same as VenomGunshipEntity/VenomDoorGunsEntity: it's a native
    // "turret"/"barrel" bone pair driven entirely by venom.json's TurretPos/BarrelPos/
    // TurretControllerIndex/TurretPitchRange/TurretYawRange, the same way Oh1Entity and
    // Mh60lEntity need zero turret-related code of their own.

    public VenomEntity(EntityType<VenomEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

}

