package frontline.combat.fcp.client.renderer.Littlebird;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.Littlebird.LittlebirdArmedModel;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdArmedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class LittlebirdArmedRenderer extends VehicleRenderer<LittlebirdArmedEntity> {
    // "door"/"door2" toggle hitboxes, matching LittlebirdArmedDoorHandler's click boxes.
    private static final AABB DOOR_BOX = new AABB(0.6764, 0.7167, -0.5263, 0.8667, 2.1095, 0.6759);
    private static final AABB DOOR2_BOX = new AABB(-0.8594, 0.7167, -0.5263, -0.6692, 2.1095, 0.6759);

    public LittlebirdArmedRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new LittlebirdArmedModel());}

    @Override
    public ResourceLocation getTextureLocation(LittlebirdArmedEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    @Override
    public void render(LittlebirdArmedEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        Minecraft mc = Minecraft.getInstance();
        if (!mc.getEntityRenderDispatcher().shouldRenderHitBoxes() || mc.options.reducedDebugInfo().get()) return;

        double root = entity.getRotateOffsetHeight();
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float roll = entity.getRoll(partialTick);
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(0, root, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        poseStack.translate(0, -root, 0);
        LevelRenderer.renderLineBox(poseStack, lines, DOOR_BOX, 0.7f, 0.3f, 1.0f, 1.0f);
        LevelRenderer.renderLineBox(poseStack, lines, DOOR2_BOX, 0.7f, 0.3f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
