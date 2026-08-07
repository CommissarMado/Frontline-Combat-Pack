package frontline.combat.fcp.client.model.JohnDeere;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CombineEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class CombineModel extends FCPVehicleModel<CombineEntity> {

    @Override
    public ResourceLocation getModelResource(CombineEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/combine.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<CombineEntity> collectTransform(String boneName) {
        // Combine: three wheel sizes. Radii in blocks, read from combine.geo.json (px / 16).
        // "...Turn" bones are the steered wheels as authored in the geo; if they steer the
        // wrong end in-game, move the Turn set to the other pair rather than editing here.
        VehicleModel.TransformContext<CombineEntity> wheels = WheelRotationTransforms.matchWheels(boneName,
                WheelRotationTransforms.steered(1.081, 30f, "WheelL0Turn", "WheelR0Turn"),
                WheelRotationTransforms.rolling(0.813, "WheelL0", "WheelR0"),
                WheelRotationTransforms.rolling(0.432, "WheelL1", "WheelR1"));
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}