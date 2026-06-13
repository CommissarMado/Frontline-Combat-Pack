package frontline.combat.fcp.client.model;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import oshi.util.tuples.Pair;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

import java.util.List;

public abstract class FCPVehicleModel <T extends VehicleEntity & GeoAnimatable> extends VehicleModel<T> {

    private int lastRenderedEntityId = Integer.MIN_VALUE;

    @Override
    public void setCustomAnimations(T vehicle, long instanceId, AnimationState<T> animationState) {
        int entityId = vehicle.getId();
        if (entityId != lastRenderedEntityId) {
            lastRenderedEntityId = entityId;
            resetSharedTransformBones();
        }
        super.setCustomAnimations(vehicle, instanceId, animationState);
    }

    private void resetSharedTransformBones() {
        List<Pair<String, TransformContext<T>>> transforms = getTRANSFORMS();
        if (transforms.isEmpty()) {
            return;
        }
        for (Pair<String, TransformContext<T>> pair : transforms) {
            CoreGeoBone bone = getAnimationProcessor().getBone(pair.getA());
            if (bone != null) {
                ModelBoneTransforms.resetForVehicleRender(bone);
            }
        }
    }
}
