package frontline.combat.fcp.client.renderer.Emplacement;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Emplacement.EmplMk19Model;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplMk19Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class EmplMk19Renderer extends VehicleRenderer<EmplMk19Entity> {
    public EmplMk19Renderer(EntityRendererProvider.Context ctx) {super(ctx, new EmplMk19Model());}
    @Override public ResourceLocation getTextureLocation(EmplMk19Entity e) {return e.getCurrentTexture();}
}
