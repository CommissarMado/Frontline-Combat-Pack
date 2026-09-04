package frontline.combat.fcp.client.renderer.Humvee;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.world.phys.Vec3;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class HumveeUnarmedRenderer extends VehicleRenderer<HumveeUnarmedEntity> {

    public HumveeUnarmedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HumveeUnarmedModel());
    }

    @Override
    public ResourceLocation getTextureLocation(HumveeUnarmedEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    @Override
    public void render(HumveeUnarmedEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        renderAttachmentHitboxes(entity, partialTick, poseStack, bufferSource);
    }

    /**
     * Draws the per-attachment interaction boxes, matching vanilla hitbox visibility (F3+B,
     * and off when reduced-debug-info is on). The pose is rebuilt to mirror SuperbWarfare's
     * getVehicleYOffsetTransform so the boxes line up with the interaction ray-cast.
     */
    private void renderAttachmentHitboxes(HumveeUnarmedEntity entity, float partialTick,
                                          PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) return;
        if (mc.options.reducedDebugInfo().get()) return;

        var categories = HumveeAttachments.categories(entity.humveeName());
        if (categories.isEmpty()) return;

        double root = entity.getRotateOffsetHeight();
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float roll = entity.getRoll(partialTick);

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());

        // ---- body-frame boxes ----
        // Rotate about the root point (translate to root, rotate, translate back), exactly
        // as SuperbWarfare's vehicleAxis does - the translate-back is what keeps the boxes
        // seated on the body instead of floating up by rotateOffsetHeight.
        poseStack.pushPose();
        poseStack.translate(0, root, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        poseStack.translate(0, -root, 0);
        for (HumveeAttachments.Category c : categories) {
            if (c.turret) continue;
            drawBox(poseStack, lines, c.aabb);
        }
        poseStack.popPose();

        // ---- turret-frame boxes (e.g. "fd"): continue into the turret's rotation so the
        // box tracks the turret. Boxes here are stored relative to the turret pivot. ----
        Vec3 turretPos = entity.getTurretPos();
        boolean anyTurret = categories.stream().anyMatch(c -> c.turret);
        if (anyTurret && turretPos != null) {
            float turretYaw = Mth.lerp(partialTick, entity.getTurretYRotO(), entity.getTurretYRot());
            poseStack.pushPose();
            poseStack.translate(0, root, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
            poseStack.translate(0, -root, 0);
            poseStack.translate(turretPos.x, turretPos.y, turretPos.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(turretYaw));
            for (HumveeAttachments.Category c : categories) {
                if (!c.turret) continue;
                drawBox(poseStack, lines, c.aabb);
            }
            poseStack.popPose();
        }
    }

    private static void drawBox(PoseStack poseStack, VertexConsumer lines, double[] a) {
        LevelRenderer.renderLineBox(poseStack, lines,
                new AABB(a[0], a[1], a[2], a[3], a[4], a[5]),
                0.7f, 0.3f, 1.0f, 1.0f);
    }
}
