package frontline.combat.fcp.client.renderer.Kamaz;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.Kamaz.KamazModel;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class KamazRenderer extends VehicleRenderer<KamazEntity> {
    public KamazRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KamazModel());
    }

    @Override
    public ResourceLocation getTextureLocation(KamazEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    private static final AABB TENT_BOX = new AABB(-1.6292, 1.0268, -2.08, 1.6335, 3.1423, 2.39);

    @Override
    public void render(KamazEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
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
        LevelRenderer.renderLineBox(poseStack, lines, TENT_BOX, 0.7f, 0.3f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
