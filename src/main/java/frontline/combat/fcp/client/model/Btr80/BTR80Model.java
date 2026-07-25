package frontline.combat.fcp.client.model.Btr80;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Btr80.BTR80Entity;
import net.minecraft.resources.ResourceLocation;
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
        // barrel is now a direct child of turret and points forward, so SBW's default barrel pitch
        // works and matches the shot at every elevation. No override needed.
        VehicleModel.TransformContext<BTR80Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "WheelTurnL1", "WheelTurnL2", "WheelTurnR1", "WheelTurnR2");
        if (turn != null) return turn;

        VehicleModel.TransformContext<BTR80Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "WheelL3", "WheelL4", "WheelR3", "WheelR2");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}