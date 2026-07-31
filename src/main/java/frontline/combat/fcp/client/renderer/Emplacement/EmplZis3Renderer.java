package frontline.combat.fcp.client.renderer.Emplacement;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Emplacement.EmplZis3Model;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplZis3Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class EmplZis3Renderer extends VehicleRenderer<EmplZis3Entity> {
    public EmplZis3Renderer(EntityRendererProvider.Context ctx) {super(ctx, new EmplZis3Model());}
    @Override public ResourceLocation getTextureLocation(EmplZis3Entity e) {return e.getCurrentTexture();}
}
