package frontline.combat.fcp.client.renderer.Trailers.ExampleTrailer;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Lav.Lav25Model;
import frontline.combat.fcp.client.model.Trailers.ExampleTrailer.ExampleTrailerModel;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ExampleTrailerRenderer extends VehicleRenderer<ExampleTrailerEntity> {

    public ExampleTrailerRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new ExampleTrailerModel());}

    @Override
    public ResourceLocation getTextureLocation(ExampleTrailerEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        } else {
            return textures[0];
        }
    }
}
