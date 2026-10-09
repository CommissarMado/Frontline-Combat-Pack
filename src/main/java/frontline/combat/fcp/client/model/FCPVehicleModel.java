package frontline.combat.fcp.client.model;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import oshi.util.tuples.Pair;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import org.jetbrains.annotations.Nullable;

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

    // ---------------------------------------------------------------------------------------------
    // Tracks
    //
    // Tracked vehicles return a shared FCPTrackPath here instead of carrying their own keyframe
    // table, so identical variants cannot drift apart. Flip a single variant without regenerating
    // anything by returning a view of the shared path:
    //
    //     return BmpTrackPaths.BMP.reversed();          // links travel the other way round the loop
    //     return BmpTrackPaths.BMP.rotationInverted();  // same positions, links spin the other way
    //     return BmpTrackPaths.BMP.rotationOffset(180); // link cube modelled the other way up
    //
    // Wheeled vehicles ignore all of this: returning null keeps SBW's default behaviour.
    // ---------------------------------------------------------------------------------------------

    /** The track path for this vehicle, or null for vehicles without tracks. */
    protected @Nullable FCPTrackPath getTrackPath() {
        return null;
    }

    @Override
    public float getBoneRotX(float t) {
        FCPTrackPath path = getTrackPath();
        return path == null ? super.getBoneRotX(t) : path.rotX(t);
    }

    @Override
    public float getBoneMoveY(float t) {
        FCPTrackPath path = getTrackPath();
        return path == null ? super.getBoneMoveY(t) : path.moveY(t);
    }

    @Override
    public float getBoneMoveZ(float t) {
        FCPTrackPath path = getTrackPath();
        return path == null ? super.getBoneMoveZ(t) : path.moveZ(t);
    }

    @Override
    public float getTrackDistance() {
        FCPTrackPath path = getTrackPath();
        return path == null ? super.getTrackDistance() : path.trackDistance();
    }
}
