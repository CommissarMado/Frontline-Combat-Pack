package frontline.combat.fcp.client.model.Uh60;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Uh60.Mh60lEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Mh60lModel extends VehicleModel<Mh60lEntity> {

    @Override
    public ResourceLocation getModelResource(Mh60lEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/mh60l.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Mh60lEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "MainRotor" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "RearRotor" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // "turret" / "barrel" (the nose camera ball) are intentionally NOT handled
            // here: the base VehicleModel already drives any bone with those exact
            // names from the vehicle's synced turret yaw / barrel pitch, driven by
            // TurretControllerIndex (seat 1, the co-pilot) - see mh60l.json. That's
            // what lets the co-pilot slew the sensor ball independently, exactly like
            // OH-1.
            //
            // Bank 1 (right side, pilot-fired "Hellfire") - hidden one at a time as
            // ammo counts down from 3 to 0.
            case "HELFIRE" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire", 3));
            case "HELFIRE2" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire", 2));
            case "HELFIRE3" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire", 1));
            case "HELFIRE4" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire", 0));
            // Bank 2 (left side, co-pilot-fired "Hellfire2").
            case "HELFIRE5" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 3));
            case "HELFIRE6" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 2));
            case "HELFIRE7" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 1));
            case "HELFIRE8" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 0));
            default -> super.collectTransform(boneName);
        };
    }
}
