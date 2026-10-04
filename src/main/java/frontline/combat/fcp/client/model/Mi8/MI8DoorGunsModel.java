package frontline.combat.fcp.client.model.Mi8;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Mi8.MI8DoorGunsEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class MI8DoorGunsModel extends VehicleModel<MI8DoorGunsEntity> {

    @Override
    public void setCustomAnimations(MI8DoorGunsEntity vehicle, long instanceId, AnimationState<MI8DoorGunsEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        this.getBone("toggle6").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle6()));
        this.getBone("toggle4").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle4()));
        this.getBone("toggle5").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle5()));
        this.getBone("toggle2").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle2()));
        this.getBone("Toggle1").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle1()));
        this.getBone("toggle3").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle3()));
        this.getBone("toggle").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle()));
        this.getBone("toggle7").ifPresent(bone -> setHiddenDeep(bone, !vehicle.hasToggle7()));

    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) setHiddenDeep(child, hidden);
    }

    @Override
    public ResourceLocation getModelResource(MI8DoorGunsEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/mi8_door_guns.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<MI8DoorGunsEntity> collectTransform(String boneName) {
        return switch (boneName) {
            // "vint" (main rotor) has a baked static yaw offset in the geo (the blade root's
            // fixed indexing angle: -17.25 degrees, confirmed identical across every Mi-8/Mi-17
            // variant's geo file). The previous version of this case read the bone's OWN current
            // value back every frame (bone.getRotY() - lerp(...)), which is the same
            // accumulate-onto-cached-state mistake the "barrel" case below avoids: this GeoBone
            // tree is the one cached instance GeckoLib keeps per model resource, never reset to
            // its baked pose between frames, so subtracting the propeller's ABSOLUTE interpolated
            // angle from last frame's already-reduced value every single frame compounds without
            // bound - the rotor doesn't just fail to coast to a stop when the engine shuts off, it
            // visibly spins faster and faster forever. Fixed the same way "barrel" below was:
            // write the baked offset as a literal constant instead of reading it back from the
            // bone, so each frame sets an absolute angle derived only from the vehicle's own
            // propeller state (matching "vint2" below, which was never broken because it already
            // did a plain absolute set with no baked offset to preserve).
            case "vint" -> {
                final float BAKED_YAW_DEG = -17.25f;
                yield (bone, vehicle, state) -> bone.setRotY(BAKED_YAW_DEG * Mth.DEG_TO_RAD - Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            }
            // "vint2" (tail rotor) has no baked rotation in the geo, so a plain replace is fine.
            case "vint2" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Single door gun (PKM) - see MI8DoorGunsEntity. "door gun" is the pintle mount (3
            // static cubes) and stays unrotated; only "barrel" (the pivot bone whose body2+magazin
            // children are the actual PKM) moves, carrying both yaw and pitch. The geo stores a
            // baked rotation of [0, -90, 0] on this bone, but the correct offset to write via
            // bone.setRotY() is empirically +90 (confirmed in-game), not -90 - written as a
            // literal constant rather than read back with bone.getRotY() and composed on top of,
            // since GeckoLib's cached GeoBone tree is never reset to its baked pose between
            // frames; see MI17DoorGunsModel for the full "spin uncontrollably" postmortem (same
            // geo/bone pattern, same fix).
            case "barrel" -> {
                final float BAKED_YAW_DEG = 90f;
                // Seat index 2 here MUST match MI8DoorGunsEntity.LEFT_GUN_SEAT and whichever
                // Seats[] entry in mi8_door_guns.json actually carries Weapons:["PKMLeft"] - a
                // mismatch doesn't error, it just silently reads yaw/pitch as 0 from an empty
                // seat every frame, which looks exactly like "the gun stopped animating".
                // Pitch NOT negated: this bone was already elevating correctly on-screen - a
                // round that negated it to "fix" a shootpos complaint was wrong, since the
                // shootpos (MI8DoorGunsEntity.getGunBarrelTransform) is an entirely separate,
                // hand-rolled JOML transform with its own independent sign, not driven by this
                // GeckoLib bone at all. See that class for the actual shootpos-side fix.
                yield (bone, vehicle, state) -> {
                    bone.setRotY(BAKED_YAW_DEG * Mth.DEG_TO_RAD + vehicle.getGunYawDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
                    bone.setRotX(vehicle.getGunPitchDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
                };
            }
            // Nose gun (PKT), copilot-controlled via SBW's own native single-turret system
            // (TurretPos/BarrelPos/TurretControllerIndex in mi8_door_guns.json - see
            // VehicleEntity.adjustTurretAngle()/turretAutoAimFromVector, zero custom aiming Java
            // needed). The raw geo already models this gun as "barrel2" (parent "mi_8MTV", its own
            // pivot, fully UV-mapped) - there's no need for a hand-made stand-in bone the way an
            // earlier round wrongly assumed. "barrel2" can't literally be renamed to "turret"/
            // "barrel" (this vehicle already has a "barrel" bone for the side door gun above, and
            // reusing the name would make both bones share whichever case runs last), so this case
            // just mirrors by hand what VehicleModel's own base collectTransform does for the
            // literal "turret"/"barrel" names (same vehicle.getTurretYRot()/getTurretXRot() fields
            // SBW already maintains). "barrel2" has no baked rotation in the geo (rotation is
            // absent, i.e. [0,0,0]), so a plain replace is correct here - no baked-constant offset
            // needed, and no compose-with-prior-frame risk either way.
            case "barrel2" ->
                    (bone, vehicle, state) -> {
                        bone.setRotY(vehicle.getTurretYRot() * Mth.DEG_TO_RAD);
                        bone.setRotX(Mth.clamp(-vehicle.getTurretXRot(), vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
                    };
            default -> super.collectTransform(boneName);
        };
    }
}
