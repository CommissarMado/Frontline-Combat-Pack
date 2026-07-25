package frontline.combat.fcp.client.model.Btr3e;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Btr3e.BTR3EEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BTR3EModel extends FCPVehicleModel<BTR3EEntity> {
    @Override public ResourceLocation getModelResource(BTR3EEntity a) { return new ResourceLocation(FCP.MODID, "geo/btr3e.geo.json"); }
    @Override public boolean hideForTurretControllerWhileZooming() { return false; }
    @Override public @Nullable VehicleModel.TransformContext<BTR3EEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<BTR3EEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f, "WheelTurnL1","WheelTurnL2","WheelTurnR1","WheelTurnR2");
        if (turn != null) return turn;
        VehicleModel.TransformContext<BTR3EEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6, "WheelL3","WheelL4","WheelR3","WheelR2");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
