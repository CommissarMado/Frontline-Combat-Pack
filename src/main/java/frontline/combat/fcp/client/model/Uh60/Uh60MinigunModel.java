package frontline.combat.fcp.client.model.Uh60;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Uh60.Uh60MinigunEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Uh60MinigunModel extends VehicleModel<Uh60MinigunEntity> {

    @Override
    public ResourceLocation getModelResource(Uh60MinigunEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/uh60_minigun.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Uh60MinigunEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "vint2" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // "turret" / "barrel" (right door gun) are auto-driven by the base
            // VehicleModel from TurretYaw/BarrelPitch, and "passengerWeaponStationYaw"
            // / "passengerWeaponStationPitch" (left door gun) are likewise auto-driven
            // from the PassengerWeaponStation system - both intentionally left to the
            // default case below. Only the two guns' independent barrel-spin needs a
            // custom case here, since that's driven by each side's own ammo tracker.
            case "GUN" ->
                    (bone, vehicle, state) -> bone.setRotZ(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotRightOld(), vehicle.getBarrelRotRight()));
            case "GUN2" ->
                    (bone, vehicle, state) -> bone.setRotZ(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotLeftOld(), vehicle.getBarrelRotLeft()));
            default -> super.collectTransform(boneName);
        };
    }
}
