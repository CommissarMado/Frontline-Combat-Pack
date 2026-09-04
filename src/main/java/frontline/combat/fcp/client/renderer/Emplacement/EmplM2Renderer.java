package frontline.combat.fcp.client.renderer.Emplacement;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Emplacement.EmplM2Model;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplM2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class EmplM2Renderer extends VehicleRenderer<EmplM2Entity> {
    public EmplM2Renderer(EntityRendererProvider.Context ctx) {super(ctx, new EmplM2Model());}
    @Override public ResourceLocation getTextureLocation(EmplM2Entity e) {return FcpVehicleTexture.resolve(e, e.getCurrentTexture());}
}
