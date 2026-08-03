package frontline.combat.fcp.client.model.Kamaz;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class KamazModel extends FCPVehicleModel<KamazEntity> {

    @Override
    public ResourceLocation getModelResource(KamazEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/kamaz.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
    @Override
    public void setCustomAnimations(KamazEntity vehicle, long instanceId, AnimationState<KamazEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("pehota2").ifPresent(b -> setHiddenDeep(b, !vehicle.hasTent()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }


    @Override
    public @Nullable TransformContext<KamazEntity> collectTransform(String boneName) {

        VehicleModel.TransformContext<KamazEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<KamazEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
