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
        return ResourceLocation.fromNamespaceAndPath(FCP.MODID, "geo/cultivator.geo.json");
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

        // The two tiller rotors (13 px across) turn with travel, but only while it is working: hitched to a tractor in
        // Farm mode.
        VehicleModel.TransformContext<CultivatorEntity> rotors =
                WheelRotationTransforms.rollingWhile(boneName, 0.406, CultivatorEntity::towedForWork, "animation", "animation2");
        if (rotors != null) return rotors;

        return super.collectTransform(boneName);
    }
}
