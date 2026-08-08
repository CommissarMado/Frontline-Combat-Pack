package frontline.combat.fcp.client.renderer.Dpv;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Dpv.DpvM240Model;
import frontline.combat.fcp.entity.vehicle.Dpv.DpvM240Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DpvM240Renderer extends VehicleRenderer<DpvM240Entity> {
    public DpvM240Renderer(EntityRendererProvider.Context ctx) {super(ctx, new DpvM240Model());}
    @Override public ResourceLocation getTextureLocation(DpvM240Entity entity) {return entity.getCurrentTexture();}
}
