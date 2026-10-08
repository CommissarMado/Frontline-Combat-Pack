package frontline.combat.fcp.client.model.Fmtv4x4;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Fmtv4x4.FMTV4x4Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class FMTV4x4Model extends VehicleModel<FMTV4x4Entity> {

    @Override
    public ResourceLocation getModelResource(FMTV4x4Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/fmtv_4x4.geo.json");
    }

    @Override
    public void setCustomAnimations(FMTV4x4Entity vehicle, long instanceId, AnimationState<FMTV4x4Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("tent").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasTent()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<FMTV4x4Entity> collectTransform(String boneName) {
        // 4x4 layout: single front axle ("wheelL0Turn"/"wheelR0Turn") steers + rolls, single rear
        // axle ("wheelL1"/"wheelR1") only rolls - the middle axle from the 6x6 FMTV is gone.
        VehicleModel.TransformContext<FMTV4x4Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.585, 30f,
                        "wheelL0Turn", "wheelR0Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<FMTV4x4Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.585,
                        "wheelL1", "wheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
