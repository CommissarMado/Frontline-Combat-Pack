package frontline.combat.fcp.client.model.Btr82;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Btr82.BTR82Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class BTR82Model extends VehicleModel<BTR82Entity> {

    @Override
    public ResourceLocation getModelResource(BTR82Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/btr82.geo.json");
    }
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<BTR82Entity> collectTransform(String boneName) {
        return switch (boneName) {

            case "WheelTurnL1", "WheelTurnR1", "WheelTurnL2", "WheelTurnR2" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));

                float steeringAngle = Mth.lerp(state.getPartialTick(), vehicle.getPrevSteeringAngle(), vehicle.getSteeringAngle());
                steeringAngle = Mth.clamp(steeringAngle, -30f, 30f);
                bone.setRotY((float) Math.toRadians(steeringAngle));
            };

            case "WheelL3", "WheelR3", "WheelL4", "WheelR4" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));
            };
            default -> super.collectTransform(boneName);
        };
    }
}
