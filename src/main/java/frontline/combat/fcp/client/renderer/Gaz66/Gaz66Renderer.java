package frontline.combat.fcp.client.renderer.Gaz66;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Gaz66.Gaz66Model;
import frontline.combat.fcp.entity.vehicle.Gaz66.Gaz66Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class Gaz66Renderer extends VehicleRenderer<Gaz66Entity> {

    public Gaz66Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new Gaz66Model());}

    @Override
    public ResourceLocation getTextureLocation(Gaz66Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
