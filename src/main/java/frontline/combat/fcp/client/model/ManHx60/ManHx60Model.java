package frontline.combat.fcp.client.model.ManHx60;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.ManHx60.ManHx60Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class ManHx60Model extends FCPVehicleModel<ManHx60Entity> {
    @Override public ResourceLocation getModelResource(ManHx60Entity animatable) {return new ResourceLocation(FCP.MODID, "geo/man_hx60.geo.json");}

    @Override
    public void setCustomAnimations(ManHx60Entity vehicle, long instanceId, AnimationState<ManHx60Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("tent unwear").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasTent()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override public boolean hideForTurretControllerWhileZooming() {return false;}

    @Override public @Nullable VehicleModel.TransformContext<ManHx60Entity> collectTransform(String boneName) {
        // 4x4 layout: single front axle ("wheel"/"wheel4") steers + rolls, single rear axle
        // ("wheel3"/"wheel6") only rolls - one fewer axle pair than the 6x6 HX58.
        VehicleModel.TransformContext<ManHx60Entity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.6562, 30f, "wheel", "wheel4");
        if (steer != null) return steer;
        VehicleModel.TransformContext<ManHx60Entity> wheels = WheelRotationTransforms.matchAny(boneName, 0.6562, "wheel3", "wheel6");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
