package frontline.combat.fcp.client.renderer.JohnDeere;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.JohnDeere.CultivatorModel;
import frontline.combat.fcp.client.model.JohnDeere.SeederModel;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CultivatorEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.SeederEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CultivatorRenderer extends GeoEntityRenderer<CultivatorEntity> {

    public CultivatorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CultivatorModel());
    }

    @Override
    protected void applyRotations(CultivatorEntity animatable, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw, float partialTick) {
        float yaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - yaw));
    }

    @Override
    public ResourceLocation getTextureLocation(CultivatorEntity entity) {
        return entity.getCurrentTexture();
    }
}
