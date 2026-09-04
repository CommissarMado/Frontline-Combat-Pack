package frontline.combat.fcp.entity.vehicle.Huey;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class HueyRocketsEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    public HueyRocketsEntity(EntityType<HueyRocketsEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

}
