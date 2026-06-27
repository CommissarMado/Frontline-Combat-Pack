package frontline.combat.fcp.client.renderer.Lav;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Lav.Lav25Model;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class Lav25Renderer extends VehicleRenderer<Lav25Entity> {

    public Lav25Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new Lav25Model());}


    @Override
    public ResourceLocation getTextureLocation(Lav25Entity entity) {
        return entity.getCurrentTexture();
    }
}