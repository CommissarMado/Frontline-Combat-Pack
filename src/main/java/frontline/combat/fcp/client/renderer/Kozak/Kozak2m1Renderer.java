package frontline.combat.fcp.client.renderer.Kozak;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Kozak.Kozak2m1Model;
import frontline.combat.fcp.entity.vehicle.Kozak.Kozak2m1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class Kozak2m1Renderer extends VehicleRenderer<Kozak2m1Entity> {
    public Kozak2m1Renderer(EntityRendererProvider.Context ctx) {super(ctx, new Kozak2m1Model());}
    @Override public ResourceLocation getTextureLocation(Kozak2m1Entity entity) {return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());}
}
