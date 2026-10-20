package frontline.combat.fcp.client.renderer.LandRover110;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.LandRover110.LandRover110Model;
import frontline.combat.fcp.entity.vehicle.LandRover110.LandRover110Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class LandRover110Renderer extends VehicleRenderer<LandRover110Entity> {

    public LandRover110Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new LandRover110Model());}

    @Override
    public ResourceLocation getTextureLocation(LandRover110Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
