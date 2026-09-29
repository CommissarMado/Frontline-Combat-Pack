package frontline.combat.fcp.client.model.Mi8;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Mi8.MI8Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class MI8Model extends VehicleModel<MI8Entity> {

    @Override
    public void setCustomAnimations(MI8Entity vehicle, long instanceId, AnimationState<MI8Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("toggle6").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle6()));
        this.getBone("toggle4").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle4()));
        this.getBone("toggle5").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle5()));
        this.getBone("toggle2").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle2()));
        this.getBone("Toggle1").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle1()));
        this.getBone("toggle3").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle3()));
        this.getBone("toggle").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle()));
        this.getBone("toggle7").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle7()));
        this.getBone("toggle8").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle8()));

    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public ResourceLocation getModelResource(MI8Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/mi8.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<MI8Entity> collectTransform(String boneName) {
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

            default -> super.collectTransform(boneName);
        };
    }
}
