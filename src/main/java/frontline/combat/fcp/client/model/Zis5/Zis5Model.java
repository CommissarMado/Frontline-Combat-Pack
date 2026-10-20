package frontline.combat.fcp.client.model.Zis5;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Zis5.Zis5Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class Zis5Model extends VehicleModel<Zis5Entity> {

    @Override
    public void setCustomAnimations(Zis5Entity vehicle, long instanceId, AnimationState<Zis5Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        // "T" is the tent frame, "TENT2" the canvas over it (both children of "tent").
        this.getBone("T").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasTentFrame()));
        this.getBone("TENT2").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasTentCanvas()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public ResourceLocation getModelResource(Zis5Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/zis5.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable TransformContext<Zis5Entity> collectTransform(String boneName) {
        // Front axle ("Whell"/"Whell3") steers + rolls; rear axle ("Whell2"/"Whell4") only rolls.
        VehicleModel.TransformContext<Zis5Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.4375, 30f, "Whell", "Whell3");
        if (turn != null) return turn;

        VehicleModel.TransformContext<Zis5Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.4375, "Whell2", "Whell4");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
