package frontline.combat.fcp.client.renderer.Brdm;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Brdm.Brdm2Model;
import frontline.combat.fcp.entity.vehicle.Brdm.Brdm2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class Brdm2Renderer extends VehicleRenderer<Brdm2Entity> {
    public Brdm2Renderer(EntityRendererProvider.Context ctx) {super(ctx, new Brdm2Model());}
    @Override public ResourceLocation getTextureLocation(Brdm2Entity e) {return e.getCurrentTexture();}
}
