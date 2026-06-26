package frontline.combat.fcp.client.renderer.Trailers.ExampleTrailer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.Trailers.ExampleTrailer.ExampleTrailerModel;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * ExampleTrailerRenderer — a plain GeckoLib renderer, deliberately NOT SBW's
 * VehicleRenderer.
 *
 * SBW's VehicleRenderer applies yaw, pitch, and roll to the model via vehicleAxis()
 * (its vehicle banking). On a setPos-driven trailer the pitch/roll inputs are
 * meaningless and spike for a single render tick, flipping the model onto its side
 * even though the entity itself (position + yaw) is stable. Rather than fight those
 * fields — whose names differ between SBW versions — this renderer extends GeckoLib's
 * GeoEntityRenderer directly, so none of SBW's render path runs at all.
 *
 * The only rotation applied is yaw. GeckoLib's default applyRotations() would emit a
 * constant 180 deg for a non-living entity (it only reads yBodyRot, which vehicles
 * don't drive), so we override it to apply (180 - yaw) instead — identical to the net
 * facing SBW produced (vehicleAxis's -yaw composed with GeckoLib's +180), so the model
 * faces the right way. No pitch, no roll: the trailer can no longer tip or bank.
 */
public class ExampleTrailerRenderer extends GeoEntityRenderer<ExampleTrailerEntity> {

    public ExampleTrailerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ExampleTrailerModel());
    }

    @Override
    protected void applyRotations(ExampleTrailerEntity animatable, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw, float partialTick) {
        float yaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - yaw));
    }

    @Override
    public ResourceLocation getTextureLocation(ExampleTrailerEntity entity) {
        return entity.getCurrentTexture();
    }
}
