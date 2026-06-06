package frontline.combat.fcp.client.renderer.Viper;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Viper.ViperModel;
import frontline.combat.fcp.entity.vehicle.Viper.ViperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ViperRenderer extends VehicleRenderer<ViperEntity> {
    public ViperRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ViperModel());
    }

    @Override
    public ResourceLocation getTextureLocation(ViperEntity entity) {
        return entity.getCurrentTexture();
    }
}