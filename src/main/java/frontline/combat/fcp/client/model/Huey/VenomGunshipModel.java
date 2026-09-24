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
            // rotation, same as Uh60MinigunModel's "barrel"/"barrel2" cases.
            case "turret1" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel1" ->
                    (bone, vehicle, state) -> bone.setRotX(vehicle.getGunPitchDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "turret2" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel2" ->
                    (bone, vehicle, state) -> bone.setRotX(vehicle.getGunPitchDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
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
            // instead, matching every other turreted FCP vehicle. The barrel's pitch axis is
            // whatever the base VehicleModel always uses (local X) - unverified in-game for this
            // specific bone; if the sensor spins/rolls instead of tilting up-down, or tilts the
            // wrong way, that's a Blockbench-side fix (give the new "barrel" bone in the geo file
            // a small rest rotation to realign its local X with true up/down), not a Java one.
            default -> super.collectTransform(boneName);
        };
    }
}
