package frontline.combat.fcp.client.renderer.Emplacement;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Emplacement.EmplMg3Model;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplMg3Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class EmplMg3Renderer extends VehicleRenderer<EmplMg3Entity> {
    public EmplMg3Renderer(EntityRendererProvider.Context ctx) {super(ctx, new EmplMg3Model());}
    @Override public ResourceLocation getTextureLocation(EmplMg3Entity e) {return e.getCurrentTexture();}
}
