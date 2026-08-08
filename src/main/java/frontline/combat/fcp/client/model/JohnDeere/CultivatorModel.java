package frontline.combat.fcp.client.model.JohnDeere;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CultivatorEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class CultivatorModel extends FCPVehicleModel<CultivatorEntity> {

    @Override
    public ResourceLocation getModelResource(CultivatorEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/cultivator.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<CultivatorEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<CultivatorEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.659, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<CultivatorEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.659,
                        "WheelL0", "WheelR0");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
