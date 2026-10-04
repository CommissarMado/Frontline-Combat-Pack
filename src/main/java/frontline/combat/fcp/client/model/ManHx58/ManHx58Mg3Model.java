package frontline.combat.fcp.client.model.ManHx58;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.ManHx58.ManHx58Mg3Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class ManHx58Mg3Model extends FCPVehicleModel<ManHx58Mg3Entity> {
    @Override public ResourceLocation getModelResource(ManHx58Mg3Entity animatable) {return new ResourceLocation(FCP.MODID, "geo/man_hx58_mg3.geo.json");}

    @Override
    public void setCustomAnimations(ManHx58Mg3Entity vehicle, long instanceId, AnimationState<ManHx58Mg3Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        // The armed variant's geo file still uses the original "tent unwear" bone name (the
        // rename to "tent2" only landed in the corrected unarmed upload).
        this.getBone("tent unwear").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasTent()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override public boolean hideForTurretControllerWhileZooming() {return false;}

    @Override public @Nullable VehicleModel.TransformContext<ManHx58Mg3Entity> collectTransform(String boneName) {
        // Same 6x6 wheel layout as the unarmed variant: front axle steers, rear tandem bogie rolls only.
        VehicleModel.TransformContext<ManHx58Mg3Entity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.6562, 30f, "wheel", "wheel4");
        if (steer != null) return steer;
        VehicleModel.TransformContext<ManHx58Mg3Entity> wheels = WheelRotationTransforms.matchAny(boneName, 0.6562, "wheel2", "wheel3", "wheel5", "wheel6");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
