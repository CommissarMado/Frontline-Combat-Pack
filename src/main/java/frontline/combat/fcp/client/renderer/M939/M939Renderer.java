package frontline.combat.fcp.client.renderer.M939;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.M939.M939Model;
import frontline.combat.fcp.entity.vehicle.M939.M939Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class M939Renderer extends VehicleRenderer<M939Entity> {
    // "tent" canopy hitbox, matching the interaction handler (M939TentHandler). Already accounts for
    // the 180-degree Y rotation baked into the "korpys" root bone.
    private static final AABB TENT_BOX = new AABB(-1.2893, 1.8761, -4.3814, 1.2291, 3.5722, 0.5525);

    public M939Renderer(EntityRendererProvider.Context ctx) {super(ctx, new M939Model());}

    @Override
    public ResourceLocation getTextureLocation(M939Entity entity) {return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());}

    @Override
    public void render(M939Entity entity, float entityYaw, float partialTick,
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
