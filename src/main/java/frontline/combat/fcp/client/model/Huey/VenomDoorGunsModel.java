package frontline.combat.fcp.client.model.Huey;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Huey.VenomDoorGunsEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

// Identical bone-transform logic to VenomGunshipModel - venom_door_guns.geo.json has the exact
// same bone hierarchy/names/pivots, only group5/group7's cube contents differ visually.
public class VenomDoorGunsModel extends VehicleModel<VenomDoorGunsEntity> {

    @Override
    public ResourceLocation getModelResource(VenomDoorGunsEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/venom_door_guns.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<VenomDoorGunsEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "glav vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // See VenomGunshipModel for the full derivation - "turret1"/"barrel1" (renamed from
            // "turret"/"barrel" to free those names up for the native turret below) is the LEFT
            // gun (seat 3), "turret2"/"barrel2" is the RIGHT gun (seat 2), and GUN2/GUN3 match.
            // Both guns rest facing forward, so pitch is a plain X rotation - NEGATED here, same
            // fix as VenomGunshipModel (the muzzle/ShootPos transform was already correct; this
            // bone posing was the one inverted).
            case "turret1" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel1" ->
                    (bone, vehicle, state) -> bone.setRotX(-vehicle.getGunPitchDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "turret2" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel2" ->
                    (bone, vehicle, state) -> bone.setRotX(-vehicle.getGunPitchDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "GUN2" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotLeftOld(), vehicle.getBarrelRotLeft()) * Mth.DEG_TO_RAD);
            case "GUN3" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotRightOld(), vehicle.getBarrelRotRight()) * Mth.DEG_TO_RAD);
            // "turret"/"barrel" (the front sensor, formerly a single hand-rolled "camera" bone)
            // are intentionally NOT handled here - see VenomGunshipModel's comment for the full
            // derivation of the "turretMount" geo fix (a baked ancestor rotation was making the
            // engine's hardcoded local-X barrel pitch twist the sensor around its own boresight
            // instead of elevating it; fixed at the geo level, not here). The base VehicleModel
            // drives turret/barrel natively from venom_door_guns.json's TurretPos/BarrelPos/
            // TurretControllerIndex/TurretPitchRange/TurretYawRange.
            default -> super.collectTransform(boneName);
        };
    }
}
