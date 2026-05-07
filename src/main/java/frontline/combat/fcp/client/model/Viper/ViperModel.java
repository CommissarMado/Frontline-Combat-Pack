package frontline.combat.fcp.client.model.Viper;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import frontline.combat.fcp.entity.vehicle.Viper.ViperEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ViperModel extends VehicleModel<ViperEntity> {

    @Override
    public ResourceLocation getModelResource(ViperEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/viper.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<ViperEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "tailPropeller" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "BarrelRotationController" ->
                    (bone, vehicle, state) -> bone.setRotZ(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRot0(), vehicle.getBarrelRot()));
            case "LockOn1" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire-LockOn", 3));
            case "LockOn2" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire-LockOn", 2));
            case "LockOn3" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire-LockOn", 1));
            case "LockOn4" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire-LockOn", 0));
            case "WireGuided1" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire-WireGuided", 3));
            case "WireGuided2" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire-WireGuided", 2));
            case "WireGuided3" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire-WireGuided", 1));
            case "WireGuided4" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire-WireGuided", 0));
            case "Sidewinder1" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Sidewinder", 1));
            case "Sidewinder2" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Sidewinder", 0));
            default -> super.collectTransform(boneName);
        };
    }
}
