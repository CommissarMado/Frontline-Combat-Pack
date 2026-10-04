package frontline.combat.fcp.client.renderer.Rg33;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Rg33.RG33Model;
import frontline.combat.fcp.entity.vehicle.Rg33.RG33Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class RG33Renderer extends VehicleRenderer<RG33Entity> {

    public RG33Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new RG33Model());}

    @Override
    public ResourceLocation getTextureLocation(RG33Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
