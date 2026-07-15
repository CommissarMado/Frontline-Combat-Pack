package frontline.combat.fcp.client.renderer.JohnDeere;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.JohnDeere.JohnDeereModel;
import frontline.combat.fcp.client.model.JohnDeere.SeederModel;
import frontline.combat.fcp.client.model.Trailers.ExampleTrailer.ExampleTrailerModel;
import frontline.combat.fcp.entity.vehicle.JohnDeere.JohnDeereEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.SeederEntity;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SeederRenderer extends GeoEntityRenderer<SeederEntity> {

    public SeederRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SeederModel());
    }

    @Override
    protected void applyRotations(SeederEntity animatable, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw, float partialTick) {
        float yaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - yaw));
    }

    @Override
    public ResourceLocation getTextureLocation(SeederEntity entity) {
        return entity.getCurrentTexture();
    }
}
