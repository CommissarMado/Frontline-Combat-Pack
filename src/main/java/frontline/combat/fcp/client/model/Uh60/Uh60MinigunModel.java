package frontline.combat.fcp.client.model.Uh60;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Uh60.Uh60MinigunEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Uh60MinigunModel extends VehicleModel<Uh60MinigunEntity> {

    @Override
    public ResourceLocation getModelResource(Uh60MinigunEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/uh60_minigun.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Uh60MinigunEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "vint2" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Neither the vehicle's built-in Turret system nor its
            // PassengerWeaponStation system is used for either gun anymore - both
            // are dead ends for this vehicle (see Uh60MinigunEntity for the full
            // reasoning: Turret is a single slot, and PassengerWeaponStation's
            // *position* is expressed inside the Turret's own rotated frame, so it
            // physically follows the main turret around - not what we want for two
            // independent door guns).
            //
            // Both guns are driven the same, symmetric way, entirely by hand:
            // Uh60MinigunEntity.getGunYawDeg()/getGunPitchDeg() compute a yaw/pitch
            // offset straight from that seat's own aim (vehicle.getShootVec(seatIndex,
            // ticks), clamped to the seat's MinYaw/MaxYaw/MinPitch/MaxPitch in
            // uh60_minigun.json) versus the weapon's rest direction
            // (vehicle.getDefaultBarrelDirection(seatIndex, ticks), reading
            // "DefaultBarrelDirection" off that seat's weapon - both guns rest facing
            // forward, [0,0,1], matching the geo's actual rest pose). This is the
            // same math SBW's own BoundBonesYaw/BoundBonesPitch renderer path uses
            // (GeoVehicleRenderer.kt), just run by hand here since that renderer
            // pipeline doesn't apply to this vehicle's GeckoLib-based rendering.
            // Left gun = seat 2, right gun = seat 3 (see uh60_minigun.json).
            //
            // Uh60MinigunEntity also registers matching "RightBarrel"/"LeftBarrel"
            // position+vector transforms (used by uh60_minigun.json's CameraPos and
            // ShootPos) built from this exact same yaw/pitch, so the camera and the
            // fired shot both track the bone's actual rotation instead of staying at
            // a fixed point.
            case "turret" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel" ->
                    (bone, vehicle, state) -> bone.setRotX(vehicle.getGunPitchDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "turret2" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel2" ->
                    (bone, vehicle, state) -> bone.setRotX(vehicle.getGunPitchDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            // "bone" / "bone2" carry a static rest-pose rotation baked into the geo
            // file itself (correcting the imported mesh to face forward) and need no
            // Java handling. Only the two guns' independent barrel-spin needs a custom
            // case here, driven by Uh60MinigunEntity.isWeaponFiring() (see that class).
            // The spinning barrel cluster rotates around its own boresight axis, which
            // for this geo's rest pose (facing forward along +Z, see "bone"/"bone2"
            // above) is the LOCAL X axis of the "GUN"/"GUN2" bone - not Z. Values are
            // fed through Mth.DEG_TO_RAD since, like setRotX/setRotY above, the bone
            // setter expects radians, not degrees.
            case "GUN" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotRightOld(), vehicle.getBarrelRotRight()) * Mth.DEG_TO_RAD);
            case "GUN2" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotLeftOld(), vehicle.getBarrelRotLeft()) * Mth.DEG_TO_RAD);
            default -> super.collectTransform(boneName);
        };
    }
}
