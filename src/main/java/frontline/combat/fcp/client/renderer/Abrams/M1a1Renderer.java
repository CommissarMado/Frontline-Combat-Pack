package frontline.combat.fcp.client.renderer.Abrams;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Abrams.M1a1Model;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class M1a1Renderer extends VehicleRenderer<M1a1Entity> {

    public M1a1Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new M1a1Model());}

    @Override
    public ResourceLocation getTextureLocation(M1a1Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
