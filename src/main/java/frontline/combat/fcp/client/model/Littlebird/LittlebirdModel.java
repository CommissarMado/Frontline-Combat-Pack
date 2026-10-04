package frontline.combat.fcp.client.model.Littlebird;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class LittlebirdModel extends VehicleModel<LittlebirdEntity> {

    @Override
    public ResourceLocation getModelResource(LittlebirdEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/littlebird.geo.json");
    }

    @Override
    public void setCustomAnimations(LittlebirdEntity vehicle, long instanceId, AnimationState<LittlebirdEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        // "door toggle" parents both side doors at once, so hiding it deep hides both.
        this.getBone("door toggle").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasDoors()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public @Nullable VehicleModel.TransformContext<LittlebirdEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "tailPropeller" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // "turret"/"barrel" (the new co-pilot camera) are intentionally NOT handled here -
            // the base VehicleModel drives them natively from littlebird.json's TurretPos/
            // BarrelPos/TurretControllerIndex/TurretPitchRange/TurretYawRange, same as the
            // Venom's front sensor and the Blackhawk's nose ball. No custom Java needed.
            default -> super.collectTransform(boneName);
        };
    }
}
