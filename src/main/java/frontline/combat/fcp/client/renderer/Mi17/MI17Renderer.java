package frontline.combat.fcp.client.renderer.Mi17;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.Mi17.MI17Model;
import frontline.combat.fcp.entity.vehicle.Mi17.MI17Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class MI17Renderer extends VehicleRenderer<MI17Entity> {
    // "toggle6" toggle hitbox, matching MI17ToggleHandler's click box.
    private static final AABB BOX_TOGGLE6 = new AABB(-1.717, 0.135, 1.2675, 1.7138, 0.9858, 1.8678);
    // "toggle4" toggle hitbox, matching MI17ToggleHandler's click box.
    private static final AABB BOX_TOGGLE4 = new AABB(-0.5917, 3.5214, -2.8323, 0.5754, 3.8131, -2.3835);
    // "toggle5" toggle hitbox, matching MI17ToggleHandler's click box.
    private static final AABB BOX_TOGGLE5 = new AABB(-1.3499, 2.4833, 0.4569, -0.7963, 2.7826, 2.4368);
    // "toggle2" toggle hitbox, matching MI17ToggleHandler's click box.
    private static final AABB BOX_TOGGLE2 = new AABB(-1.4139, 2.9696, -1.1291, 1.4106, 3.7178, 0.2773);
    // "Toggle1" toggle hitbox, matching MI17ToggleHandler's click box.
    private static final AABB BOX_TOGGLE1 = new AABB(-1.2384, 1.0169, 2.8223, 1.2352, 1.6741, 3.9245);
    // "toggle3" toggle hitbox, matching MI17ToggleHandler's click box.
    private static final AABB BOX_TOGGLE3 = new AABB(-1.2942, 2.431, -3.0443, 1.2909, 2.7452, -1.9072);
    // "toggle" toggle hitbox, matching MI17ToggleHandler's click box.
    private static final AABB BOX_TOGGLE = new AABB(-0.5151, 3.0335, 2.1715, 0.513, 3.3328, 2.3209);

    public MI17Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MI17Model());
    }

    @Override
    public ResourceLocation getTextureLocation(MI17Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    @Override
    public void render(MI17Entity entity, float entityYaw, float partialTick,
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
        LevelRenderer.renderLineBox(poseStack, lines, BOX_TOGGLE6, 0.7f, 0.3f, 1.0f, 1.0f);
        LevelRenderer.renderLineBox(poseStack, lines, BOX_TOGGLE4, 0.7f, 0.3f, 1.0f, 1.0f);
        LevelRenderer.renderLineBox(poseStack, lines, BOX_TOGGLE5, 0.7f, 0.3f, 1.0f, 1.0f);
        LevelRenderer.renderLineBox(poseStack, lines, BOX_TOGGLE2, 0.7f, 0.3f, 1.0f, 1.0f);
        LevelRenderer.renderLineBox(poseStack, lines, BOX_TOGGLE1, 0.7f, 0.3f, 1.0f, 1.0f);
        LevelRenderer.renderLineBox(poseStack, lines, BOX_TOGGLE3, 0.7f, 0.3f, 1.0f, 1.0f);
        LevelRenderer.renderLineBox(poseStack, lines, BOX_TOGGLE, 0.7f, 0.3f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
