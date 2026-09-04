package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.VenomModel;
import frontline.combat.fcp.entity.vehicle.Huey.VenomEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class VenomRenderer extends VehicleRenderer<VenomEntity> {
    public VenomRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new VenomModel());}

    @Override
    public ResourceLocation getTextureLocation(VenomEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
