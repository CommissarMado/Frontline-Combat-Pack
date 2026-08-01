package frontline.combat.fcp.entity.vehicle.Emplacement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EmplMk19Entity extends ClampedTurretEntity {
    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/emplacements/mk19.png"),
            new ResourceLocation("fcp", "textures/entity/emplacements/mk19.png")
    };
    private static final String[] CAMO_NAMES = {"Default"};
    public EmplMk19Entity(EntityType<EmplMk19Entity> type, Level world) {super(type, world);}
    @Override public ResourceLocation[] getCamoTextures() {return CAMO_TEXTURES;}
    @Override public String[] getCamoNames() {return CAMO_NAMES;}
    @Override public com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier getDamageModifier() {return super.getDamageModifier().custom((s,dmg) -> getSourceAngle(s, 0.4f) * dmg);}

    @Override protected double[] legOffset() { return new double[]{0.5, 0.1, -0.7}; }
    @Override protected boolean needsManualReload() { return false; }
}
