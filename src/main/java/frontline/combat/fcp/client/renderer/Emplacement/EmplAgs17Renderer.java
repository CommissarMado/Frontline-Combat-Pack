package frontline.combat.fcp.client.renderer.Emplacement;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Emplacement.EmplAgs17Model;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplAgs17Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class EmplAgs17Renderer extends VehicleRenderer<EmplAgs17Entity> {
    public EmplAgs17Renderer(EntityRendererProvider.Context ctx) {super(ctx, new EmplAgs17Model());}
    @Override public ResourceLocation getTextureLocation(EmplAgs17Entity e) {return e.getCurrentTexture();}
}
