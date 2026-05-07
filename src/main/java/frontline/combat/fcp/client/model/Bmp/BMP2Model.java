package frontline.combat.fcp.client.model.Bmp;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP2Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class BMP2Model extends VehicleModel<BMP2Entity> {

    @Override
    public ResourceLocation getModelResource(BMP2Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/bmp2.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<BMP2Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "WheelL0", "WheelR0", "WheelL1", "WheelR1", "WheelL2", "WheelR2", "WheelL3", "WheelR3",
                 "WheelL4", "WheelR4", "WheelL5", "WheelR5", "WheelL6", "WheelR6", "WheelL7", "WheelR7" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));
            };
            default -> super.collectTransform(boneName);
        };
    }
}
