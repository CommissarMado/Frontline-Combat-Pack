package frontline.combat.fcp.client.model.T80bvm;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.entity.vehicle.T80bvm.T80BVMEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class T80BVMModel extends VehicleModel<T80BVMEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<T80BVMEntity> collectTransform(String boneName) {
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
