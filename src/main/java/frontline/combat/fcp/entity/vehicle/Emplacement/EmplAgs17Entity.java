package frontline.combat.fcp.entity.vehicle.Emplacement;

import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EmplAgs17Entity extends CamoVehicleBase {
    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/emplacements/ags17.png"),
            new ResourceLocation("fcp", "textures/entity/emplacements/ags17.png")
    };
    private static final String[] CAMO_NAMES = {"Default"};
    public EmplAgs17Entity(EntityType<EmplAgs17Entity> type, Level world) {super(type, world);}
    @Override public ResourceLocation[] getCamoTextures() {return CAMO_TEXTURES;}
    @Override public String[] getCamoNames() {return CAMO_NAMES;}
    @Override public com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier getDamageModifier() {return super.getDamageModifier().custom((s,dmg) -> getSourceAngle(s, 0.4f) * dmg);}
}
