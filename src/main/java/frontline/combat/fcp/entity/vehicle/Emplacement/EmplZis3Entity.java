package frontline.combat.fcp.entity.vehicle.Emplacement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EmplZis3Entity extends ClampedTurretEntity {
    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/emplacements/zis3.png"),
            new ResourceLocation("fcp", "textures/entity/emplacements/zis3.png")
    };
    private static final String[] CAMO_NAMES = {"Default"};
    public EmplZis3Entity(EntityType<? extends com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity> type, Level world) {super(type, world);}
    @Override public ResourceLocation[] getCamoTextures() {return CAMO_TEXTURES;}
    @Override public String[] getCamoNames() {return CAMO_NAMES;}

    @Override protected double[] legOffset() { return new double[]{1.5, 0.1, -1.8}; }
}
