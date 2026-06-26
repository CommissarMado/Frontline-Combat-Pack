package frontline.combat.fcp.client.model.Trailers.ExampleTrailer;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ExampleTrailerModel extends FCPVehicleModel<ExampleTrailerEntity> {

    @Override
    public ResourceLocation getModelResource(ExampleTrailerEntity animatable) {
        // Placeholder geo — swap for your trailer's own model.
        return new ResourceLocation(FCP.MODID, "geo/lav25.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<ExampleTrailerEntity> collectTransform(String boneName) {

        VehicleModel.TransformContext<ExampleTrailerEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.5, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<ExampleTrailerEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.5,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}