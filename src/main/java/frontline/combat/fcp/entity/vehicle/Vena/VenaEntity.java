package frontline.combat.fcp.entity.vehicle.Vena;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoArtilleryBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 2S31 Vena: BMP-3 hull with a 120 mm 2A80 gun-mortar turret. Built on the same artillery base as the
 * Msta and M109 (CamoArtilleryBase), so it takes the Artillery Indicator, solves its own trajectories
 * and autoloads its "Main" gun from the hold.
 */
public class VenaEntity extends CamoArtilleryBase {

    public int INVENTORY_SIZE = 9;

    public VenaEntity(EntityType<VenaEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
}
