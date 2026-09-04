package frontline.combat.fcp.client.renderer.Kozak;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Kozak.Kozak5Model;
import frontline.combat.fcp.entity.vehicle.Kozak.Kozak5Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class Kozak5Renderer extends VehicleRenderer<Kozak5Entity> {
    public Kozak5Renderer(EntityRendererProvider.Context ctx) {super(ctx, new Kozak5Model());}
    @Override public ResourceLocation getTextureLocation(Kozak5Entity entity) {return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());}
}
