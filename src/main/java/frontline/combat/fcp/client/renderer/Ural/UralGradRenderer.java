package frontline.combat.fcp.client.renderer.Ural;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ural.UralGradModel;
import frontline.combat.fcp.entity.vehicle.Ural.UralGradEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class UralGradRenderer extends VehicleRenderer<UralGradEntity> {
    public UralGradRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new UralGradModel());
    }

    @Override
    public ResourceLocation getTextureLocation(UralGradEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
