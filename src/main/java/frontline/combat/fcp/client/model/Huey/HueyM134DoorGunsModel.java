package frontline.combat.fcp.client.model.Huey;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM134DoorGunsEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class HueyM134DoorGunsModel extends VehicleModel<HueyM134DoorGunsEntity> {

    @Override
    public ResourceLocation getModelResource(HueyM134DoorGunsEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/huey_m134.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HueyM134DoorGunsEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "glav vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // "turret2"/"barrel2" is the RIGHT gun (seat 2), "turret"/"barrel" is the LEFT gun
            // (seat 3) - see HueyM60DoorGunsEntity's class comment for the full derivation (same
            // architecture, this geo just has an extra spinning tube cluster per side). Both sides
            // now driven by custom Java, same sign convention on both.
            case "turret2" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel2" ->
                    (bone, vehicle, state) -> bone.setRotX(-vehicle.getGunPitchDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "turret" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel" ->
                    (bone, vehicle, state) -> bone.setRotX(-vehicle.getGunPitchDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            // Barrel-cluster spin, same pattern as VenomGunshipModel's GUN2/GUN3: a plain local-X
            // boresight spin driven by isWeaponFiring, purely cosmetic regardless of ancestor
            // rotation. minigun15069 is the LEFT (turret/barrel) chain's tube cluster, minigun7 is
            // the RIGHT (turret2/barrel2) chain's.
            case "minigun15069" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotLeftOld(), vehicle.getBarrelRotLeft()) * Mth.DEG_TO_RAD);
            case "minigun7" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotRightOld(), vehicle.getBarrelRotRight()) * Mth.DEG_TO_RAD);
            default -> super.collectTransform(boneName);
        };
    }
}
