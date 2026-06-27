package frontline.combat.fcp.client.renderer.MemeVehicles;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.MemeVehicles.WolfModel;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.WolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class WolfRenderer extends VehicleRenderer<WolfEntity> {

    public WolfRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new WolfModel());}

    @Override
    public ResourceLocation getTextureLocation(WolfEntity entity) {
        return entity.getCurrentTexture();
    }
}
