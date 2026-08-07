package frontline.combat.fcp.client.model.NovatorUnarmed;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.NovatorUnarmed.NovatorUnarmedEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class NovatorUnarmedModel extends FCPVehicleModel<NovatorUnarmedEntity> {
    @Override
    public ResourceLocation getModelResource(NovatorUnarmedEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/novator.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {return false;}

    // Unarmed variant: hide the remote weapon station ("rws" -> turret -> barrel -> gun) entirely.
    @Override
    public void setCustomAnimations(NovatorUnarmedEntity vehicle, long instanceId, AnimationState<NovatorUnarmedEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("rws").ifPresent(b -> setHiddenDeep(b, true));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public @Nullable VehicleModel.TransformContext<NovatorUnarmedEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<NovatorUnarmedEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.621, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<NovatorUnarmedEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.621,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
