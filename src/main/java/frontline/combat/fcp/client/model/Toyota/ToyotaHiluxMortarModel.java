package frontline.combat.fcp.client.model.Toyota;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxMortarEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

// VehicleModel (not FCPVehicleModel): keeps the barrel's [0,180,0] bind rotation that aims the
// tube forward; FCPVehicleModel's reset would strip it. Same mortar as the Stryker.
public class ToyotaHiluxMortarModel extends VehicleModel<ToyotaHiluxMortarEntity> {

    @Override
    public ResourceLocation getModelResource(ToyotaHiluxMortarEntity a) {
        return new ResourceLocation(FCP.MODID, "geo/toyota_hilux_mortar.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<ToyotaHiluxMortarEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<ToyotaHiluxMortarEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<ToyotaHiluxMortarEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
