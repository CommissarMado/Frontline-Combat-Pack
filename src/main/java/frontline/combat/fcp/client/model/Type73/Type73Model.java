package frontline.combat.fcp.client.model.Type73;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeUnarmedEntity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.AnimationState;

/**
 * Type 73 light truck (type73, type73_m2) on the shared attachment entity, like the Dingo. The geo is picked from the
 * registry name; the normal one's "toggle" (rear hard top) is a removable hull attachment.
 */
public class Type73Model extends VehicleModel<HumveeUnarmedEntity> {

    /** Tyre radius in blocks, from the wheel bones. */
    private static final double WHEEL_RADIUS = 0.388;

    @Override
    public ResourceLocation getModelResource(HumveeUnarmedEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + animatable.humveeName() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HumveeUnarmedEntity> collectTransform(String boneName) {
        // Front axle steers and rolls; the rear one only rolls.
        VehicleModel.TransformContext<HumveeUnarmedEntity> front =
                WheelRotationTransforms.matchAnyTurn(boneName, WHEEL_RADIUS, 30f, "wheel_fl", "wheel_fr");
        if (front != null) return front;
        VehicleModel.TransformContext<HumveeUnarmedEntity> rear =
                WheelRotationTransforms.matchAny(boneName, WHEEL_RADIUS, "wheel_rl", "wheel_rr");
        if (rear != null) return rear;

        return super.collectTransform(boneName);
    }

    @Override
    public void setCustomAnimations(HumveeUnarmedEntity vehicle, long instanceId, AnimationState<HumveeUnarmedEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
