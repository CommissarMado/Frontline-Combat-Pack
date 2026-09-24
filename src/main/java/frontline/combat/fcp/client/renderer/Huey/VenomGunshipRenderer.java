package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.VenomGunshipModel;
import frontline.combat.fcp.entity.vehicle.Huey.VenomGunshipEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class VenomGunshipRenderer extends VehicleRenderer<VenomGunshipEntity> {
    public VenomGunshipRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new VenomGunshipModel());}

    @Override
    public ResourceLocation getTextureLocation(VenomGunshipEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
