package frontline.combat.fcp.client.model.ManHx58;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.ManHx58.ManHx58Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class ManHx58Model extends FCPVehicleModel<ManHx58Entity> {
    @Override public ResourceLocation getModelResource(ManHx58Entity animatable) {return new ResourceLocation(FCP.MODID, "geo/man_hx58.geo.json");}

    @Override
    public void setCustomAnimations(ManHx58Entity vehicle, long instanceId, AnimationState<ManHx58Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("tent2").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasTent()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override public boolean hideForTurretControllerWhileZooming() {return false;}

    @Override public @Nullable VehicleModel.TransformContext<ManHx58Entity> collectTransform(String boneName) {
        // Front axle ("wheel"/"wheel4") steers + rolls; the two rear axles ("wheel2"/"wheel5" and
        // "wheel3"/"wheel6") only roll, matching the truck's 6x6 layout (front steer, tandem rear bogie).
        VehicleModel.TransformContext<ManHx58Entity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.6562, 30f, "wheel", "wheel4");
        if (steer != null) return steer;
        VehicleModel.TransformContext<ManHx58Entity> wheels = WheelRotationTransforms.matchAny(boneName, 0.6562, "wheel2", "wheel3", "wheel5", "wheel6");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
