package frontline.combat.fcp.client.renderer.Puma;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Puma.PumaModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Puma.PumaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PumaRenderer extends VehicleRenderer<PumaEntity> {

    public PumaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PumaModel());
    }

    @Override
    public ResourceLocation getTextureLocation(PumaEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
