package frontline.combat.fcp.client.model.Mi17;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Mi17.MI17DoorGunsEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class MI17DoorGunsModel extends VehicleModel<MI17DoorGunsEntity> {

    @Override
    public void setCustomAnimations(MI17DoorGunsEntity vehicle, long instanceId, AnimationState<MI17DoorGunsEntity> animationState) {
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
    public ResourceLocation getModelResource(MI17DoorGunsEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/mi17_door_guns.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<MI17DoorGunsEntity> collectTransform(String boneName) {
        return switch (boneName) {
            // "vint" (main rotor) has a baked static yaw offset in the geo (the blade root's
            // fixed indexing angle: -17.25 degrees, confirmed identical across every Mi-8/Mi-17
            // variant's geo file). The previous version of this case read the bone's OWN current
            // value back every frame (bone.getRotY() - lerp(...)), which is the same
            // accumulate-onto-cached-state mistake the "barrel"/"barrel2" cases below avoid: this
            // GeoBone tree is the one cached instance GeckoLib keeps per model resource, never
            // reset to its baked pose between frames, so subtracting the propeller's ABSOLUTE
            // interpolated angle from last frame's already-reduced value every single frame
            // compounds without bound - the rotor doesn't just fail to coast to a stop when the
            // engine shuts off, it visibly spins faster and faster forever. Fixed the same way
            // "barrel"/"barrel2" below were: write the baked offset as a literal constant instead
            // of reading it back from the bone, so each frame sets an absolute angle derived only
            // from the vehicle's own propeller state (matching "vint2" below, which was never
            // broken because it already did a plain absolute set with no baked offset to
            // preserve).
            case "vint" -> {
                final float BAKED_YAW_DEG = -17.25f;
                yield (bone, vehicle, state) -> bone.setRotY(BAKED_YAW_DEG * Mth.DEG_TO_RAD - Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            }
            // "vint2" (tail rotor) has no baked rotation in the geo, so a plain replace is fine.
            case "vint2" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Two independent door guns (PKMs) - see MI17DoorGunsEntity for the full yaw/pitch
            // derivation. "door gun"/"door_gun2" are the pintle MOUNTS (3 static cubes each) and
            // stay completely unrotated - only the actual gun (the "barrel"/"barrel2" pivot bone
            // and its body2+magazin / body3+magazin2 children, i.e. the PKM itself) moves. Both
            // yaw and pitch are therefore applied to "barrel"/"barrel2" together. The geo stores
            // a baked rotation of [0, -90, 0] on this bone, but GeckoLib's bone.setRotY() doesn't
            // apply that raw value directly - empirically (confirmed in-game) the correct offset
            // to write here is +90, not -90. This is written as a literal constant (BAKED_YAW_DEG)
            // rather than read back with bone.getRotY() and composed on top of,
            // because this GeoBone tree is the single cached instance GeckoLib keeps per model
            // resource (GeckoLibCache.getBakedModels()), reused across every frame and every
            // instance of this entity - it is never reset to its baked pose between frames. A
            // continuously-accumulating read-modify-write (bone.getRotY() + delta) is correct
            // only for a value that's meant to keep growing forever (see "vint" below, where the
            // ever-increasing propeller rotation IS the whole point); applied to an absolute aim
            // angle that's recomputed fresh every frame, it stacks the same offset on top of
            // itself frame after frame and spins the gun out of control. Writing the baked
            // constant plus the fresh offset avoids reading prior frame state entirely.
            // Pitch sign for "barrel"/"barrel2" is a purely VISUAL (GeckoLib bone) matter, entirely
            // separate from the gameplay shootpos math in MI17DoorGunsEntity.getGunBarrelTransform
            // (whose own pitchSign parameter handles that side's flip independently - see that
            // class). A round that conflated the two and negated "barrel" here to "fix" a shootpos
            // complaint was wrong: this bone (seat 3/PKMLeft) was already elevating correctly
            // on-screen and must be left alone. Only "barrel2" (seat 2/PKMRight, the mirrored
            // geometry) needs the negation - confirmed in-game, see its own comment below.
            case "barrel" -> {
                final float BAKED_YAW_DEG = 90f;
                yield (bone, vehicle, state) -> {
                    bone.setRotY(BAKED_YAW_DEG * Mth.DEG_TO_RAD + vehicle.getGunYawDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
                    bone.setRotX(vehicle.getGunPitchDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
                };
            }
            case "barrel2" -> {
                final float BAKED_YAW_DEG = 90f;
                // barrel2 is body2/"barrel"'s mirror image (negative-X pivot vs positive-X), and
                // mirrored geometry flips the sense of local-X rotation - confirmed in-game: pitch
                // needs to be negated here, unlike "barrel" above, or the right gun's elevation
                // reads backwards (aiming up visually pitches it down and vice versa). Yaw doesn't
                // need the same treatment - only pitch was reported as inverted.
                yield (bone, vehicle, state) -> {
                    bone.setRotY(BAKED_YAW_DEG * Mth.DEG_TO_RAD + vehicle.getGunYawDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
                    bone.setRotX(-vehicle.getGunPitchDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
                };
            }
            default -> super.collectTransform(boneName);
        };
    }
}
