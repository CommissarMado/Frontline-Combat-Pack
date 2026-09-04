package frontline.combat.fcp.client.renderer.Trailers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

/**
 * Base renderer for trailers, the counterpart of SBW's VehicleRenderer for driveables.
 * A concrete trailer renderer is just a constructor:
 *
 *   public class SeederRenderer extends TrailerRenderer<SeederEntity> {
 *       public SeederRenderer(EntityRendererProvider.Context ctx) {
 *           super(ctx, new SeederModel());
 *       }
 *   }
 *
 * Deliberately NOT VehicleRenderer itself: that applies driving pitch/bank/roll visuals,
 * which a towed follower flattens every tick — yaw is the only rotation a trailer has.
 * Texture resolves through the camo system, so sprayed camos and the wrecked state work
 * on every trailer automatically.
 */
public abstract class TrailerRenderer<T extends AbstractTrailerEntity> extends GeoEntityRenderer<T> {

    protected TrailerRenderer(EntityRendererProvider.Context ctx, GeoModel<T> model) {
        super(ctx, model);
    }

    @Override
    protected void applyRotations(T animatable, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw, float partialTick) {
        // Pin correction FIRST, while the pose axes are still world-aligned: shifts the
        // model so the tongue sits exactly on the driver's rendered hitch this frame.
        // Without it the linearly-interpolated pose lets the tongue drift off the hitch
        // mid-frame, worse the longer the hitch/tongue offsets are.
        net.minecraft.world.phys.Vec3 pin = animatable.renderPinCorrection(partialTick);
        poseStack.translate(pin.x, pin.y, pin.z);

        float yaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        float pitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - yaw));
        // Terrain pitch from the entity. Negated: the 180° yaw flip above mirrors the X
        // axis, and vanilla xRot is positive-down while the flipped model needs the inverse.
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}