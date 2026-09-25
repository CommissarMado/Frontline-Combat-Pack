package frontline.combat.fcp.client.model.Oh1;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Oh1.Oh1Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class Oh1Model extends VehicleModel<Oh1Entity> {

    @Override
    public void setCustomAnimations(Oh1Entity vehicle, long instanceId, AnimationState<Oh1Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("toggle1").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle1()));
        this.getBone("toggle2").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle2()));
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public ResourceLocation getModelResource(Oh1Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/oh1.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Oh1Entity> collectTransform(String boneName) {
        return switch (boneName) {
            // Main rotor spins about the mast (Y axis), driven by the shared engine
            // propeller rotation value every helicopter already has.
            case "MainRotor" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Tail rotor spins about a lateral axis (X); geared faster than the main
            // rotor, matching the x6 ratio FCP's other helicopters use for their tail rotors.
            case "RearRotor" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // "turret" / "barrel" are intentionally NOT handled here: the base VehicleModel
            // already drives any bone with those exact names from the vehicle's synced
            // turret yaw / barrel pitch, which is in turn driven by whichever seat
            // TurretControllerIndex points at (the co-pilot, seat 1 - see oh1.json). That's
            // what lets the co-pilot slew the nose sensor ball independently of the pilot.
            default -> super.collectTransform(boneName);
        };
    }
}
