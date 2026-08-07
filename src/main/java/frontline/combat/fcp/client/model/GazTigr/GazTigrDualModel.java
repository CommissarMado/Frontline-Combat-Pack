package frontline.combat.fcp.client.model.GazTigr;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrDualEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class GazTigrDualModel extends FCPVehicleModel<GazTigrDualEntity> {

    @Override
    public ResourceLocation getModelResource(GazTigrDualEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/gaz_tigr_dual.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable TransformContext<GazTigrDualEntity> collectTransform(String boneName) {

        // Front wheels (front axle, Z ~ -29) steer: roll on X from travel AND pivot on Y from the
        // vehicle's steering angle. The dual geo has no separate "...Turn" bones, so the wheel bones
        // themselves both roll and steer (as on the base chassis' front bones).
        VehicleModel.TransformContext<GazTigrDualEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "whell", "whell2");
        if (turn != null) return turn;

        // Rear wheels (rear axle, Z ~ +25) just roll.
        VehicleModel.TransformContext<GazTigrDualEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "whell3", "whell4");
        if (wheels != null) return wheels;

        // The PKM is the primary weapon (rides the native "Barrel" transform so the gunner camera
        // elevates correctly), but its bone is "barrel2", not "barrel", so SuperbWarfare won't
        // auto-pitch it. Pitch it here with the same angle/clamp the base model applies to "barrel".
        if ("barrel2".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float xRot = Mth.lerp(state.getPartialTick(), vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                bone.setRotX(Mth.clamp(-xRot, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
            };
        }

        // Turret yaw ("turret") and the AGS barrel pitch ("barrel") are handled by
        // SuperbWarfare's base VehicleModel via super.
        return super.collectTransform(boneName);
    }
}
