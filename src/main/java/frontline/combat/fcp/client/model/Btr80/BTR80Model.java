package frontline.combat.fcp.client.model.Btr80;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Btr80.BTR80Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class BTR80Model extends FCPVehicleModel<BTR80Entity> {

    @Override
    public ResourceLocation getModelResource(BTR80Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/btr80.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<BTR80Entity> collectTransform(String boneName) {
        // The gun tube geometry points backward on this model (unlike the BTR-82), so the default
        // barrel transform aimed it in reverse. Yaw the barrel 180 to face forward and drive the
        // pitch in that frame; the shot (forward reconstruction) already matched. If the elevation
        // ends up inverted, negate the rotX (use clamp(xr, ...) or a leading minus).
        if ("barrel".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float xr = Mth.lerp((float) state.getPartialTick(), vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                bone.setRotY((float) Math.PI);
                bone.setRotX(Mth.clamp(-xr, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
            };
        }

        // Front wheels steer + roll (matchAnyTurn does both: roll on X, pivot on Y from steering angle).
        VehicleModel.TransformContext<BTR80Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "WheelTurnL1", "WheelTurnL2", "WheelTurnR1", "WheelTurnR2");
        if (turn != null) return turn;

        // Rear wheels just roll.
        VehicleModel.TransformContext<BTR80Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "WheelL3", "WheelL4", "WheelR3", "WheelR2");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}