package frontline.combat.fcp.client.renderer.Uh60;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Uh60.Uh60Model;
import frontline.combat.fcp.entity.vehicle.Uh60.Uh60Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class Uh60Renderer extends VehicleRenderer<Uh60Entity> {

    public Uh60Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Uh60Model());
    }

    @Override
    public ResourceLocation getTextureLocation(Uh60Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
