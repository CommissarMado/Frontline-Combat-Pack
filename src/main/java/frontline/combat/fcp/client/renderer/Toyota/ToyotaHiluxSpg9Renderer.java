package frontline.combat.fcp.client.renderer.Toyota;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Toyota.ToyotaHiluxSpg9Model;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxSpg9Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ToyotaHiluxSpg9Renderer extends VehicleRenderer<ToyotaHiluxSpg9Entity> {

    public ToyotaHiluxSpg9Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new ToyotaHiluxSpg9Model());}

    @Override
    public ResourceLocation getTextureLocation(ToyotaHiluxSpg9Entity entity) {
        return entity.getCurrentTexture();
    }
}
