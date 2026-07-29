package frontline.combat.fcp.client.renderer.M939;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.M939.M939Model;
import frontline.combat.fcp.entity.vehicle.M939.M939Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class M939Renderer extends VehicleRenderer<M939Entity> {
    public M939Renderer(EntityRendererProvider.Context ctx) {super(ctx, new M939Model());}
    @Override public ResourceLocation getTextureLocation(M939Entity entity) {return entity.getCurrentTexture();}
}
