package frontline.combat.fcp.client.renderer.Oh1;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.Oh1.Oh1Model;
import frontline.combat.fcp.entity.vehicle.Oh1.Oh1Entity;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class Oh1Renderer extends VehicleRenderer<Oh1Entity> {
    // "toggle1"/"toggle2" pod hitboxes, matching Oh1ToggleHandler's click boxes.
    private static final AABB TOGGLE1_BOX = new AABB(0.7447, 0.7955, -0.7083, 1.1634, 1.5173, 0.8769);
    private static final AABB TOGGLE2_BOX = new AABB(-1.1634, 0.7955, -0.7083, -0.7447, 1.5173, 0.8769);

    public Oh1Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Oh1Model());
    }

    @Override
    public ResourceLocation getTextureLocation(Oh1Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    @Override
    public void render(Oh1Entity entity, float entityYaw, float partialTick,
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
        LevelRenderer.renderLineBox(poseStack, lines, TOGGLE1_BOX, 0.7f, 0.3f, 1.0f, 1.0f);
        LevelRenderer.renderLineBox(poseStack, lines, TOGGLE2_BOX, 0.7f, 0.3f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
