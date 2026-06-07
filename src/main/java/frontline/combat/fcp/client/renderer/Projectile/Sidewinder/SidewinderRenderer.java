package frontline.combat.fcp.client.renderer.Projectile.Sidewinder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.Projectile.Hellfire.LockOnHellfireModel;
import frontline.combat.fcp.client.model.Projectile.Sidewinder.SidewinderModel;
import frontline.combat.fcp.entity.projectile.Hellfire.LockOnHellfireEntity;
import frontline.combat.fcp.entity.projectile.Sidewinder.SidewinderEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SidewinderRenderer extends GeoEntityRenderer<SidewinderEntity> {

    public SidewinderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SidewinderModel());
    }

    @Override
    public RenderType getRenderType(SidewinderEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void render(SidewinderEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
        poseStack.popPose();
    }
}
