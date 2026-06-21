package frontline.combat.fcp.client.renderer.Trailers.ExampleTrailer;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Trailers.ExampleTrailer.ExampleTrailerModel;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * ExampleTrailerRenderer — nothing special needed. The trailer's yaw is set
 * server-side every tick and interpolated by SBW's VehicleRenderer using the
 * entity's own yRotO→yRot, so there is no render() override here.
 */
public class ExampleTrailerRenderer extends VehicleRenderer<ExampleTrailerEntity> {

    public ExampleTrailerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ExampleTrailerModel());
    }

    @Override
    public ResourceLocation getTextureLocation(ExampleTrailerEntity entity) {
        return entity.getCurrentTexture();
    }
}