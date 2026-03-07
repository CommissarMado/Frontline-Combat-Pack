package frontline.combat.fcp.client.model.T72av;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.T72av.T72AVEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class T72AVModel extends VehicleModel<T72AVEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<T72AVEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "wheelL0", "wheelR0", "wheelL1", "wheelR1", "wheelL2", "wheelR2", "wheelL3", "wheelR3",
                 "wheelL4", "wheelR4", "wheelL5", "wheelR5", "wheelL6", "wheelR6", "wheelL7", "wheelR7" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));
            };
            default -> super.collectTransform(boneName);
        };
    }
}
