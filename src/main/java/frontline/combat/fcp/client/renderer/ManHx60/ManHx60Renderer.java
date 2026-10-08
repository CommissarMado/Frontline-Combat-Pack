package frontline.combat.fcp.client.renderer.ManHx60;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.ManHx60.ManHx60Model;
import frontline.combat.fcp.entity.vehicle.ManHx60.ManHx60Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class ManHx60Renderer extends VehicleRenderer<ManHx60Entity> {
    // "tent unwear" canopy hitbox, matching the interaction handler (ManHx60TentHandler). Shrunk
    // from the HX58's tent box to match the HX60's shorter 4x4 chassis (one fewer axle out back).
    private static final AABB TENT_BOX = new AABB(-1.4058, 2.1317, -4.501, 1.4071, 4.0763, 2.265);

    public ManHx60Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ManHx60Model());
    }

    @Override
    public ResourceLocation getTextureLocation(ManHx60Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    @Override
    public void render(ManHx60Entity entity, float entityYaw, float partialTick,
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
        LevelRenderer.renderLineBox(poseStack, lines, TENT_BOX, 0.7f, 0.3f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
