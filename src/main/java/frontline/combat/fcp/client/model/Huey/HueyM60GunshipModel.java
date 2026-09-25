package frontline.combat.fcp.client.model.Huey;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM60GunshipEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

// Identical bone-transform logic to HueyM60DoorGunsModel - huey_m60_gunship.geo.json has the same
// bone hierarchy/names/pivots for the rotor and gun bones, differing only in rocket-pod geometry
// (which needs no custom bone driving, wired purely via the vehicle JSON's pilot-seat Rocket weapon).
public class HueyM60GunshipModel extends VehicleModel<HueyM60GunshipEntity> {

    @Override
    public ResourceLocation getModelResource(HueyM60GunshipEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/huey_m60_gunship.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HueyM60GunshipEntity> collectTransform(String boneName) {
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
            default -> super.collectTransform(boneName);
        };
    }
}
