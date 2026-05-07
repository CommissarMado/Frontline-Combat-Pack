package frontline.combat.fcp.client.model.Stryker;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Novator.NovatorEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerM2Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMGSEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class StrykerM2Model extends VehicleModel<StrykerM2Entity> {

    @Override
    public ResourceLocation getModelResource(StrykerM2Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/stryker_m2.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<StrykerM2Entity> collectTransform(String boneName) {
        return switch (boneName) {

            case "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));

                float steeringAngle = Mth.lerp(state.getPartialTick(), vehicle.getPrevSteeringAngle(), vehicle.getSteeringAngle());
                steeringAngle = Mth.clamp(steeringAngle, -30f, 30f);
                bone.setRotY((float) Math.toRadians(steeringAngle));
            };

            case "WheelL0", "WheelR0", "WheelL1", "WheelR1" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));
            };
            default -> super.collectTransform(boneName);
        };
    }
}
