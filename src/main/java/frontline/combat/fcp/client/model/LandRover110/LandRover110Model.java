package frontline.combat.fcp.client.model.LandRover110;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.LandRover110.LandRover110Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class LandRover110Model extends VehicleModel<LandRover110Entity> {

    @Override
    public ResourceLocation getModelResource(LandRover110Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/landrover_110.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<LandRover110Entity> collectTransform(String boneName) {
        // Front axle ("WHELL"/"WHELL2") steers + rolls; rear axle ("WHELL3"/"WHELL4") only rolls.
        VehicleModel.TransformContext<LandRover110Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.425, 30f, "WHELL", "WHELL2");
        if (turn != null) return turn;

        VehicleModel.TransformContext<LandRover110Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.425, "WHELL3", "WHELL4");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
