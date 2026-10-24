package frontline.combat.fcp.client.model.Toyota;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeUnarmedEntity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import software.bernie.geckolib.core.animation.AnimationState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * Toyota HMV (Kokido / High Mobility Vehicle). The model was built facing +Z, so the geo wraps it in a
 * "root" bone turned 180 degrees. Its wheel bones are modelled turned 180 as well; together with the
 * root that cancels out, so the rolling wheels animate as normal - only the steered ones, whose Y
 * rotation the steering overwrites, need their 180 put back.
 * <p>
 * Shares the Humvee attachment entity: Attachments/Top switches between the hard cab top and the
 * canvas tent (see fcp_humvee_attachments.json).
 */
public class ToyotaHmvModel extends VehicleModel<HumveeUnarmedEntity> {

    /** Tyre radius in blocks (wheel hitboxes are 15 px tall). */
    private static final double WHEEL_RADIUS = 0.47;

    @Override
    public ResourceLocation getModelResource(HumveeUnarmedEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/toyota_hmv.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HumveeUnarmedEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<HumveeUnarmedEntity> front =
                WheelRotationTransforms.matchAnyTurn(boneName, WHEEL_RADIUS, 30f, "wheel", "wheel3");
        if (front != null) {
            return (bone, vehicle, state) -> {
                front.transform(bone, vehicle, state);
                bone.setRotY(bone.getRotY() + Mth.PI);
            };
        }
        VehicleModel.TransformContext<HumveeUnarmedEntity> rear =
                WheelRotationTransforms.matchAny(boneName, WHEEL_RADIUS, "wheel2", "wheel4");
        if (rear != null) return rear;

        return super.collectTransform(boneName);
    }

    /** Attachment "Top": the hard cab top ("zakrityi top") or the full canvas tent, never both. */
    @Override
    public void setCustomAnimations(HumveeUnarmedEntity vehicle, long instanceId, AnimationState<HumveeUnarmedEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
