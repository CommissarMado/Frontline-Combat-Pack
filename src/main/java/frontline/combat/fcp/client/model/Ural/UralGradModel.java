package frontline.combat.fcp.client.model.Ural;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralGradEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class UralGradModel extends VehicleModel<UralGradEntity> {

    @Override
    public ResourceLocation getModelResource(UralGradEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/ural_grad.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<UralGradEntity> collectTransform(String boneName) {
        return switch (boneName) {
            // Передние колёса с поворотом (wheelL0Turn, wheelR0Turn)
            case "WheelL0Turn", "WheelR0Turn" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));

                float steeringAngle = Mth.lerp(state.getPartialTick(), vehicle.getPrevSteeringAngle(), vehicle.getSteeringAngle());
                steeringAngle = Mth.clamp(steeringAngle, -30f, 30f);
                bone.setRotY((float) Math.toRadians(steeringAngle));
            };
            // Задние колёса - только вращение
            case "WheelL1", "WheelR1", "WheelL0", "WheelR0" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));
            };
            default -> super.collectTransform(boneName);
        };
    }
}
