package frontline.combat.fcp.client.renderer.Oh1;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Oh1.Oh1Model;
import frontline.combat.fcp.entity.vehicle.Oh1.Oh1Entity;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class Oh1Renderer extends VehicleRenderer<Oh1Entity> {
    public Oh1Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Oh1Model());
    }

    @Override
    public ResourceLocation getTextureLocation(Oh1Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
