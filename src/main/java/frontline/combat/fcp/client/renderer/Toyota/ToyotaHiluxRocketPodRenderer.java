package frontline.combat.fcp.client.renderer.Toyota;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Toyota.ToyotaHiluxRocketPodModel;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxRocketPodEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ToyotaHiluxRocketPodRenderer extends VehicleRenderer<ToyotaHiluxRocketPodEntity> {
    public ToyotaHiluxRocketPodRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new ToyotaHiluxRocketPodModel());}

    @Override
    public ResourceLocation getTextureLocation(ToyotaHiluxRocketPodEntity entity) {
        return entity.getCurrentTexture();
    }
}
