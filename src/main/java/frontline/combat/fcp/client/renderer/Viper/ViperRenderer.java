package frontline.combat.fcp.client.renderer.Viper;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Littlebird.LittlebirdArmedModel;
import frontline.combat.fcp.client.model.Viper.ViperModel;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdArmedEntity;
import frontline.combat.fcp.entity.vehicle.Viper.ViperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ViperRenderer extends VehicleRenderer<ViperEntity> {
    public ViperRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new ViperModel());}

    @Override
    public ResourceLocation getTextureLocation(ViperEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
