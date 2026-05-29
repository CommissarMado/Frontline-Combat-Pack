package frontline.combat.fcp.client.renderer.Trailers.ExampleTrailer;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import frontline.combat.fcp.client.model.Lav.Lav25Model;
import frontline.combat.fcp.client.model.Trailers.ExampleTrailer.ExampleTrailerModel;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

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

    @Override
    public void render(ExampleTrailerEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        // Use TRAILER_YAW lerped by partial tick for the model's base rotation.
        // Mth.rotLerp handles the 360°/0° wraparound correctly.
        // This overrides whatever GeckoLib would have read from entity.getYRot()
        // or entity.getViewYRot(), ensuring rotation is always authoritative.
        float smoothYaw = Mth.rotLerp(partialTick, entity.prevTrailerYaw, entity.getTrailerYaw());

        super.render(entity, smoothYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
