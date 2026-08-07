package frontline.combat.fcp.client.model.Ural;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class UralModel extends VehicleModel<UralEntity> {

    @Override
    public void setCustomAnimations(UralEntity vehicle, long instanceId, AnimationState<UralEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("TENTY").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasTent()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public ResourceLocation getModelResource(UralEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/ural.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<UralEntity> collectTransform(String boneName) {

        VehicleModel.TransformContext<UralEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.586, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<UralEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.586,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
