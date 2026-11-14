package frontline.combat.fcp.client.renderer.Abrams;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frontline.combat.fcp.client.model.Abrams.M1a1Model;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.client.renderer.VehicleCamoNet;
import frontline.combat.fcp.client.renderer.Humvee.HumveeUnarmedRenderer;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;

public class M1a1Renderer extends VehicleRenderer<M1a1Entity> {

    private final VehicleCamoNet camoNet = new VehicleCamoNet();

    public M1a1Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new M1a1Model());}

    @Override
    public ResourceLocation getTextureLocation(M1a1Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    /** Also draws the camo net (after the model, from the bone frames it captured) and the attachment interaction boxes (F3+B), as on the Humvees. */
    @Override
    public void render(M1a1Entity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        camoNet.begin(entity);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        camoNet.draw(entity, bufferSource, packedLight);
        HumveeUnarmedRenderer.renderAttachmentHitboxes(entity, partialTick, poseStack, bufferSource);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, M1a1Entity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  int colour) {
        if (!isReRender) camoNet.capture(poseStack, bone);
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}
