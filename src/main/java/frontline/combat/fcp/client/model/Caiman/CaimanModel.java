package frontline.combat.fcp.client.model.Caiman;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeUnarmedEntity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.AnimationState;

/**
 * One model shared by every Caiman MRAP variant. Like the Humvees, the Caimans run on the shared
 * attachment entity: the geo is picked from the registry name, and the "toggle" (roof rack) and
 * "toggle1" (gun shield) bones are attachments that can be removed.
 */
public class CaimanModel extends VehicleModel<HumveeUnarmedEntity> {

    /** Tyre radius in blocks, from the wheel hitboxes (x1.07 resized model). */
    private static final double WHEEL_RADIUS = 0.635;

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
        // Front axle steers and rolls; the rear tandem axles only roll.
        VehicleModel.TransformContext<HumveeUnarmedEntity> front =
                WheelRotationTransforms.matchAnyTurn(boneName, WHEEL_RADIUS, 30f, "whell", "whell2");
        if (front != null) return front;
        VehicleModel.TransformContext<HumveeUnarmedEntity> rear =
                WheelRotationTransforms.matchAny(boneName, WHEEL_RADIUS, "whell3", "whell4", "whell5", "whell6");
        if (rear != null) return rear;

        return super.collectTransform(boneName);
    }

    @Override
    public void setCustomAnimations(HumveeUnarmedEntity vehicle, long instanceId, AnimationState<HumveeUnarmedEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
