package frontline.combat.fcp.client.model.GazTigr;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrGLEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrMGEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrRWSEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class GazTigrGLModel extends VehicleModel<GazTigrGLEntity> {

    @Override
    public ResourceLocation getModelResource(GazTigrGLEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/gaz_tigr_gl.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable TransformContext<GazTigrGLEntity> collectTransform(String boneName) {
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
