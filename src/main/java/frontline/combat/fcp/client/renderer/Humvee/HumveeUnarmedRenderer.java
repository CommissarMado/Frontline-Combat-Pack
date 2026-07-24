package frontline.combat.fcp.client.renderer.Humvee;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frontline.combat.fcp.client.model.Humvee.HumveeUnarmedModel;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeUnarmedEntity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class HumveeUnarmedRenderer extends VehicleRenderer<HumveeUnarmedEntity> {

    private static final double HITBOX_HALF = 0.5; // ~1 block cube

    public HumveeUnarmedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HumveeUnarmedModel());
    }

    @Override
    public ResourceLocation getTextureLocation(HumveeUnarmedEntity entity) {
        return entity.getCurrentTexture();
    }

    @Override
    public void render(HumveeUnarmedEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        renderAttachmentHitboxes(entity, entityYaw, partialTick, poseStack, bufferSource);
    }

    /** Draws the per-attachment interaction boxes, matching vanilla hitbox visibility rules. */
    private void renderAttachmentHitboxes(HumveeUnarmedEntity entity, float entityYaw, float partialTick,
                                          PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) return;
        if (mc.options.reducedDebugInfo().get()) return;

        var categories = HumveeAttachments.categories(entity.humveeName());
        if (categories.isEmpty()) return;

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        float bodyYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());

        poseStack.pushPose();
        // Orient into the vehicle's local frame so the boxes ride the body as it turns.
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-bodyYaw));
        for (HumveeAttachments.Category c : categories) {
            double x = c.hitbox[0];
            double y = c.hitbox[1];
            double z = c.hitbox[2];
            AABB box = new AABB(x - HITBOX_HALF, y - HITBOX_HALF, z - HITBOX_HALF,
                    x + HITBOX_HALF, y + HITBOX_HALF, z + HITBOX_HALF);
            LevelRenderer.renderLineBox(poseStack, lines, box, 0.25f, 1.0f, 0.35f, 1.0f);
        }
        poseStack.popPose();
    }
}
