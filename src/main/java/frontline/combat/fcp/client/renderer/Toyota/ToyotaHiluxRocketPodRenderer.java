package frontline.combat.fcp.client.renderer.Toyota;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Toyota.ToyotaHiluxRocketPodModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxRocketPodEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ToyotaHiluxRocketPodRenderer extends FCPVehicleRenderer<ToyotaHiluxRocketPodEntity> {
    public ToyotaHiluxRocketPodRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new ToyotaHiluxRocketPodModel());}

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(ToyotaHiluxRocketPodEntity entity) {
        return entity.getCurrentTexture();
    }
}
