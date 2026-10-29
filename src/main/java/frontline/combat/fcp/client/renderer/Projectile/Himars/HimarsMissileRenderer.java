package frontline.combat.fcp.client.renderer.Projectile.Himars;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.HimarsTvClient;
import frontline.combat.fcp.client.model.Projectile.Himars.HimarsMissileModel;
import frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HimarsMissileRenderer extends GeoEntityRenderer<HimarsMissileEntity> {

    public HimarsMissileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HimarsMissileModel());
    }

    @Override
    public RenderType getRenderType(HimarsMissileEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void render(HimarsMissileEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        if (HimarsTvClient.isPiloted(entityIn)) return; // the camera is inside it
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
        poseStack.popPose();
    }
}
