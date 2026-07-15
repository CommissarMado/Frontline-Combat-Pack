package frontline.combat.fcp.client.renderer.Toyota;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Toyota.ToyotaHiluxZu23Model;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxZu23Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ToyotaHiluxZu23Renderer extends VehicleRenderer<ToyotaHiluxZu23Entity> {

    public ToyotaHiluxZu23Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new ToyotaHiluxZu23Model());}

    @Override
    public ResourceLocation getTextureLocation(ToyotaHiluxZu23Entity entity) {
        return entity.getCurrentTexture();
    }
}
