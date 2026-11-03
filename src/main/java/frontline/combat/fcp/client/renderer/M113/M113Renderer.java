package frontline.combat.fcp.client.renderer.M113;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.M113.M113Model;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.M113.M113Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class M113Renderer extends VehicleRenderer<M113Entity> {

    public M113Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new M113Model()); }

    @Override
    public ResourceLocation getTextureLocation(M113Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
