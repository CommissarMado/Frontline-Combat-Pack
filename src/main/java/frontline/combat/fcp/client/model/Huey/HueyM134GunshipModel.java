package frontline.combat.fcp.client.model.Huey;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM134GunshipEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

// Identical bone-transform logic to HueyM134DoorGunsModel - huey_m134_gunship.geo.json has the
// same bone hierarchy/names/pivots for the rotor, gun, and tube-cluster bones, differing only in
// rocket-pod geometry (wired purely via the vehicle JSON's pilot-seat Rocket weapon).
public class HueyM134GunshipModel extends VehicleModel<HueyM134GunshipEntity> {

    @Override
    public ResourceLocation getModelResource(HueyM134GunshipEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/huey_m134_gunship.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HueyM134GunshipEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "glav vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "turret2" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel2" ->
                    (bone, vehicle, state) -> bone.setRotX(-vehicle.getGunPitchDeg(2, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "turret" ->
                    (bone, vehicle, state) -> bone.setRotY(vehicle.getGunYawDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel" ->
                    (bone, vehicle, state) -> bone.setRotX(-vehicle.getGunPitchDeg(3, state.getPartialTick()) * Mth.DEG_TO_RAD);
            case "minigun15069" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotLeftOld(), vehicle.getBarrelRotLeft()) * Mth.DEG_TO_RAD);
            case "minigun7" ->
                    (bone, vehicle, state) -> bone.setRotX(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotRightOld(), vehicle.getBarrelRotRight()) * Mth.DEG_TO_RAD);
            default -> super.collectTransform(boneName);
        };
    }
}
