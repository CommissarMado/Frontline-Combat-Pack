package frontline.combat.fcp.client.model.M939;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.M939.M939Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class M939Model extends FCPVehicleModel<M939Entity> {
    @Override public ResourceLocation getModelResource(M939Entity animatable) {return new ResourceLocation(FCP.MODID, "geo/m939.geo.json");}

    @Override
    public void setCustomAnimations(M939Entity vehicle, long instanceId, AnimationState<M939Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("tent").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasTent()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<M939Entity> collectTransform(String boneName) {
        // Front axle steers + rolls; the two rear axles only roll (WheelLTurn8 is a spare, left static).
        VehicleModel.TransformContext<M939Entity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.7, 30f, "WheelLTurn5", "WheelLTurn6");
        if (steer != null) return steer;
        VehicleModel.TransformContext<M939Entity> wheels = WheelRotationTransforms.matchAny(boneName, 0.7, "WheelLTurn7", "WheelLTurn2", "WheelLTurn3", "WheelLTurn4");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
