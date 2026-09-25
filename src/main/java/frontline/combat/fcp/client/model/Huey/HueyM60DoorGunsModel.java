package frontline.combat.fcp.client.model.Huey;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM60DoorGunsEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class HueyM60DoorGunsModel extends VehicleModel<HueyM60DoorGunsEntity> {

    @Override
    public ResourceLocation getModelResource(HueyM60DoorGunsEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/huey_m60.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HueyM60DoorGunsEntity> collectTransform(String boneName) {
        return switch (boneName) {
            // Same rotor bones/pivots as the Venom family's "glav vint"/"vint" convention.
            case "glav vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // "turret2"/"barrel2" is the RIGHT gun (seat 2), "turret"/"barrel" is the LEFT gun
            // (seat 3) - see HueyM60DoorGunsEntity's class comment for the derivation. Both now
            // driven by custom Java (matching VenomGunshipModel's turret1/barrel1 + turret2/
            // barrel2 pattern) - same sign convention on both sides (yaw not negated, pitch
            // negated), verified against a matching Ry(180) ancestor chain.
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
