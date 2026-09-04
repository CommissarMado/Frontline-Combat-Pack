package frontline.combat.fcp.entity.vehicle.Emplacement;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EmplKornetEntity extends EmplacementEntity {

    /** Manual loading only - never pulls ammo from the gunner's inventory. */
    @Override
    protected boolean usesInventoryReload() {
        return false;
    }
    public EmplKornetEntity(EntityType<EmplKornetEntity> type, Level world) {super(type, world);}
    @Override public DamageModifier getDamageModifier() {return super.getDamageModifier().custom((entity, s, dmg) -> getSourceAngle(s, 0.4f) * dmg);}
}