package frontline.combat.fcp.client.renderer.M109;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.M109.M109Model;
import frontline.combat.fcp.entity.vehicle.M109.M109Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class M109Renderer extends VehicleRenderer<M109Entity> {

    public M109Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new M109Model());}

    @Override
    public ResourceLocation getTextureLocation(M109Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}

