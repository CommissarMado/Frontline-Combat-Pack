package frontline.combat.fcp.client.renderer.M113;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.M113.M113Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class M113Renderer extends VehicleRenderer<M113Entity> {

    /** Each variant has its own model class (M113Model, M113A2Model, SkifModel); see ModEntityRenderers. */
    public M113Renderer(EntityRendererProvider.Context renderManager, FCPVehicleModel<M113Entity> model) { super(renderManager, model); }

    @Override
    public ResourceLocation getTextureLocation(M113Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
