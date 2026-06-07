package frontline.combat.fcp.client.renderer.MemeVehicles;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.MemeVehicles.BigBirdModel;
import frontline.combat.fcp.client.model.MemeVehicles.LaHumveeModel;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.BigBirdEntity;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.LaHumveeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LaHumveeRenderer extends VehicleRenderer<LaHumveeEntity> {
    public LaHumveeRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new LaHumveeModel());}

    @Override
    public ResourceLocation getTextureLocation(LaHumveeEntity entity) {
        return entity.getCurrentTexture();
    }
}
