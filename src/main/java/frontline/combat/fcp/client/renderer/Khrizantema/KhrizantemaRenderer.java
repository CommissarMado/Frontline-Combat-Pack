package frontline.combat.fcp.client.renderer.Khrizantema;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Khrizantema.KhrizantemaModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Khrizantema.KhrizantemaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class KhrizantemaRenderer extends VehicleRenderer<KhrizantemaEntity> {

    public KhrizantemaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KhrizantemaModel());
    }

    @Override
    public ResourceLocation getTextureLocation(KhrizantemaEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
