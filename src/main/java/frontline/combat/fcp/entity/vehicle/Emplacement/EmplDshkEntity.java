package frontline.combat.fcp.entity.vehicle.Emplacement;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EmplDshkEntity extends CamoVehicleBase {
    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/emplacements/dshk.png"),
            new ResourceLocation("fcp", "textures/entity/emplacements/dshk.png")
    };
    private static final String[] CAMO_NAMES = {"Default"};
    public EmplDshkEntity(EntityType<EmplDshkEntity> type, Level world) {super(type, world);}
    @Override public ResourceLocation[] getCamoTextures() {return CAMO_TEXTURES;}
    @Override public String[] getCamoNames() {return CAMO_NAMES;}
    @Override public DamageModifier getDamageModifier() {return super.getDamageModifier().custom((s,dmg) -> getSourceAngle(s, 0.4f) * dmg);}

    @Override public boolean isPushable() { return false; }
}
