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
            // Grouped by ROW (top/bottom on each rack), not by rack side - matching
            // Viper's pilot=LockOn(bottom) / co-pilot=WireGuided(top) split.
            //
            // "Hellfire" (pilot, LockOn-style, drop-down) - the bottom missile on each
            // rack: HELFIRE3/4 (right rack) and HELFIRE7/8 (left rack). Hide order
            // matches mh60l.json's ShootPos.Positions order for this weapon.
            case "HELFIRE3" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire", 3));
            case "HELFIRE4" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire", 2));
            case "HELFIRE7" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire", 1));
            case "HELFIRE8" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire", 0));
            // "Hellfire2" (co-pilot, WireGuided-style, fires straight forward) - the
            // top missile on each rack: HELFIRE/HELFIRE2 (right rack) and
            // HELFIRE5/HELFIRE6 (left rack).
            case "HELFIRE" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 3));
            case "HELFIRE2" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 2));
            case "HELFIRE5" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 1));
            case "HELFIRE6" ->
                    (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Hellfire2", 0));
            default -> super.collectTransform(boneName);
        };
    }
}
