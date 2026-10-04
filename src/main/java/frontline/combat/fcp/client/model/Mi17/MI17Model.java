package frontline.combat.fcp.client.model.Mi17;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Mi17.MI17Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class MI17Model extends VehicleModel<MI17Entity> {

    @Override
    public void setCustomAnimations(MI17Entity vehicle, long instanceId, AnimationState<MI17Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("toggle6").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle6()));
        this.getBone("toggle4").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle4()));
        this.getBone("toggle5").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle5()));
        this.getBone("toggle2").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle2()));
        this.getBone("Toggle1").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle1()));
        this.getBone("toggle3").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle3()));
        this.getBone("toggle").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle()));

    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public ResourceLocation getModelResource(MI17Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/mi17.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<MI17Entity> collectTransform(String boneName) {
        return switch (boneName) {
            // "vint" (main rotor) has a baked static yaw offset in the geo (the blade root's
            // fixed indexing angle: -17.25 degrees, confirmed identical across every Mi-8/Mi-17
            // variant's geo file). The previous version of this case read the bone's OWN current
            // value back every frame (bone.getRotY() - lerp(...)), which is the same
            // accumulate-onto-cached-state mistake other variants' door-gun "barrel" cases avoid:
            // this GeoBone tree is the one cached instance GeckoLib keeps per model resource,
            // never reset to its baked pose between frames, so subtracting the propeller's
            // ABSOLUTE interpolated angle from last frame's already-reduced value every single
            // frame compounds without bound - the rotor doesn't just fail to coast to a stop when
            // the engine shuts off, it visibly spins faster and faster forever. Fixed by writing
            // the baked offset as a literal constant instead of reading it back from the bone, so
            // each frame sets an absolute angle derived only from the vehicle's own propeller
            // state (matching "vint2" below, which was never broken because it already did a
            // plain absolute set with no baked offset to preserve).
            case "vint" -> {
                final float BAKED_YAW_DEG = -17.25f;
                yield (bone, vehicle, state) -> bone.setRotY(BAKED_YAW_DEG * Mth.DEG_TO_RAD - Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            }
            // "vint2" (tail rotor) has no baked rotation in the geo, so a plain replace is fine.
            case "vint2" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Chin-mounted EO/IR observation ball - the same two-bone gimbal (separate yaw ring +
            // pitching camera housing) as MI8AMTShEntity's sighting camera, and literally the same
            // pivots (this geo was copied from the AMTSh's, just with "turret"/"barrel" renamed to
            // "cameraturret"/"camerabarrel" - presumably so they wouldn't collide with a future
            // armed variant's own "turret"/"barrel"). VehicleModel's base collectTransform already
            // drives a bone literally named "turret"/"barrel" from vehicle.getTurretYRot()/
            // getTurretXRot() (that's what AMTSh relies on via its `default ->` fallthrough, no
            // override needed there) - but it only matches those literal names, so a renamed pair
            // needs its own case here doing the exact same thing by hand. Split across two bones
            // (yaw on the parent, pitch on the child), NOT combined onto one like
            // MI8DoorGunsModel's nose-gun "barrel2" hack - that hack only existed because Mi-8's
            // door-gun "barrel" name was already taken; there's no such collision here, and the
            // real two-bone rig should be driven the same way VehicleModel's own default does it.
            case "cameraturret" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getTurretYRot() * Mth.DEG_TO_RAD);
            case "camerabarrel" ->
                    (bone, vehicle, state) -> bone.setRotX(Mth.clamp(-vehicle.getTurretXRot(), vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);

            default -> super.collectTransform(boneName);
        };
    }
}
