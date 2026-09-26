package frontline.combat.fcp.client.model.Littlebird;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Littlebird.Ah6mLittlebirdEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class Ah6mLittlebirdModel extends VehicleModel<Ah6mLittlebirdEntity> {

    @Override
    public ResourceLocation getModelResource(Ah6mLittlebirdEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/ah6m_littlebird.geo.json");
    }

    @Override
    public void setCustomAnimations(Ah6mLittlebirdEntity vehicle, long instanceId, AnimationState<Ah6mLittlebirdEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        // "door toggle" parents both side doors at once, so hiding it deep hides both.
        this.getBone("door toggle").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasDoors()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Ah6mLittlebirdEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "tailPropeller" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Only one minigun barrel survives on this variant (the right-hand minigun/rocket
            // pod pair was replaced by the Hellfire rack), so there's just one controller bone
            // to spin, unlike littlebird_armed's two.
            case "BarrelRotationController1" ->
                    (bone, vehicle, state) -> bone.setRotZ(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRot0(), vehicle.getBarrelRot()));
            // "turret"/"barrel" (the new co-pilot camera) are intentionally NOT handled here -
            // the base VehicleModel drives them natively from ah6m_littlebird.json's TurretPos/
            // BarrelPos/TurretControllerIndex/TurretPitchRange/TurretYawRange, same as the
            // Venom's front sensor and the Blackhawk's nose ball. No custom Java needed.
            //
            // Hellfire2 (co-pilot, wire-guided, fired via the camera turret) - hide each missile
            // bone once its shot has been fired, matching the ShootPos.Positions order in
            // ah6m_littlebird.json (Positions[0]=HELFIRE, Positions[1]=HELFIRE2), exactly the
            // same "count down from Magazine-1 to 0" mechanism the Blackhawk (Mh60lModel) uses
            // for its own Hellfire2 rack.
            case "HELFIRE" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 1));
            case "HELFIRE2" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 0));
            default -> super.collectTransform(boneName);
        };
    }
}
