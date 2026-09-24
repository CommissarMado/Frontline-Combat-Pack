package frontline.combat.fcp.client.model.Huey;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Huey.VenomGunshipEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class VenomGunshipModel extends VehicleModel<VenomGunshipEntity> {

    @Override
    public ResourceLocation getModelResource(VenomGunshipEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/venom_gunship.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<VenomGunshipEntity> collectTransform(String boneName) {
        return switch (boneName) {
            // Same rotor bones as the base Venom, just renamed by whatever tool exported this
            // geo file ("glav vint" = main rotor, matches "propeller"'s pivot exactly; "vint" =
            // tail rotor, matches "tailPropeller"'s pivot exactly - confirmed by comparing pivots
            // against venom.geo.json bone-for-bone).
            case "glav vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Dual independent door guns, same approach as Uh60MinigunModel/Uh60MinigunEntity.
            // Renamed to "turret1"/"barrel1" (was "turret"/"barrel") to free up the literal
            // names "turret"/"barrel" for the front sensor's native turret below. "turret1"/
            // "barrel1" is the LEFT gun, seat 3; "turret2"/"barrel2" is the RIGHT gun, seat 2 -
            // see VenomGunshipEntity's class comment for the full derivation (properly composing
            // Uh-1's ancestor rotation flips which bone poses to which side of the vehicle,
            // versus a naive reading of the two bones' raw, unposed model-space X sign).
            //
            // Both guns rest facing forward like the UH-60's, so elevation/pitch is a plain X
            // rotation - but NEGATED here, unlike Uh60MinigunModel's "barrel"/"barrel2" cases.
            // Confirmed in-game: the ShootPos/muzzle-flash transform (VenomGunshipEntity's
            // getGunBarrelTransform) was already elevating the correct way; this bone posing was
            // the one that was inverted, showing the barrel visually tilting opposite to where it
            // was actually shooting. Negating here (instead of touching the already-correct
            // Matrix4d transform) brings the two back into agreement.
            case "turret1" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel1" ->
                    (bone, vehicle, state) -> bone.setRotX(-vehicle.getGunPitchDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "turret2" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel2" ->
                    (bone, vehicle, state) -> bone.setRotX(-vehicle.getGunPitchDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            // Barrel-cluster spin, same pattern as the UH-60's GUN/GUN2 (local X boresight,
            // driven by isWeaponFiring). GUN2 is part of the turret1/barrel1 (LEFT) chain, GUN3
            // is part of the turret2/barrel2 (RIGHT) chain.
            case "GUN2" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotLeftOld(), vehicle.getBarrelRotLeft()) * Mth.DEG_TO_RAD);
            case "GUN3" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotRightOld(), vehicle.getBarrelRotRight()) * Mth.DEG_TO_RAD);
            // "turret"/"barrel" are intentionally NOT handled here (same as Oh1Model): the base
            // VehicleModel already drives any bone with those exact names from the vehicle's
            // synced turret yaw / barrel pitch, which is in turn driven by whichever seat
            // TurretControllerIndex points at (the co-pilot, seat 1 - see venom_gunship.json's
            // TurretPos/BarrelPos/TurretControllerIndex/TurretPitchRange/TurretYawRange). The
            // front sensor bone used to be a single hand-rolled "camera" bone driven by custom
            // Java code; it's now split into this native turret("turret")/barrel("barrel") pair
            // instead, matching every other turreted FCP vehicle.
            //
            // The engine hardcodes barrel pitch as a local-X rotation (VehicleModel.kt's
            // "barrel" -> bone.rotX = ...), with zero per-vehicle configurability. "turret"'s
            // ancestor chain here (Uh-1's baked [0,180,0] plus group8's own [0,-90,0]) composes
            // to a net Ry(90 deg) arriving at "turret"'s frame - and a 90-degree Y rotation is
            // exactly the case where local X stops being a sideways/pitch axis and becomes the
            // BORESIGHT axis instead (verified via full forward kinematics: mapped local X/Y/Z
            // unit vectors through the composed chain into vehicle space). That's why the sensor
            // was twisting/rolling in place instead of tilting up-down - it wasn't a small rest-
            // rotation problem, the engine was rotating around the barrel's own forward axis.
            // Fixed at the geo level (not here) by inserting a static "turretMount" bone between
            // "not mirrored" and "turret" with rotation [0,-90,0], which cancels group8's -90
            // contribution and brings the cumulative rotation at "turret"'s frame back to
            // identity - restoring local X as a true sideways/pitch axis, matching every other
            // working turret in this pack. Barrel's pivot and both bones' cube geometry were
            // counter-rotated by the inverse so the rest-pose appearance is pixel-identical to
            // before; TurretPos/BarrelPos in venom_gunship.json were recomputed against the new
            // geo (numerically confirmed unchanged, since the visual pose was preserved exactly).
            // This has NOT been visually confirmed in-game/Blockbench - please check that the
            // sensor now elevates instead of twisting, and that up/down isn't inverted (if it is,
            // that's a one-line sign fix: flip turretMount's rotation to [0,90,0] instead).
            default -> super.collectTransform(boneName);
        };
    }
}
