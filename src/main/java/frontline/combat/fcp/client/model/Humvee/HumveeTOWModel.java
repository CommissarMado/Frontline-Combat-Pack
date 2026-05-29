package frontline.combat.fcp.client.model.Humvee;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeEntity;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeTOWEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class HumveeTOWModel extends VehicleModel<HumveeTOWEntity> {
    @Override
    public ResourceLocation getModelResource(HumveeTOWEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/humvee_tow.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<HumveeTOWEntity> collectTransform(String boneName) {
        return switch (boneName) {

            case "WheelL0Turn", "WheelR0Turn" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));

                float steeringAngle = Mth.lerp(state.getPartialTick(), vehicle.getPrevSteeringAngle(), vehicle.getSteeringAngle());
                steeringAngle = Mth.clamp(steeringAngle, -30f, 30f);
                bone.setRotY((float) Math.toRadians(steeringAngle));
            };

            case "WheelL0", "WheelR0" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());

                bone.setRotX((float) Math.toRadians(-wheelRot));
            };
            default -> super.collectTransform(boneName);
        };
    }
}
