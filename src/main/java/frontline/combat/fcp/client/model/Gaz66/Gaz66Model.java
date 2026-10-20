package frontline.combat.fcp.client.model.Gaz66;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Gaz66.Gaz66Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Gaz66Model extends VehicleModel<Gaz66Entity> {

    @Override
    public ResourceLocation getModelResource(Gaz66Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/gaz66.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Gaz66Entity> collectTransform(String boneName) {
        // Front axle ("whell"/"whell2") steers + rolls; rear axle ("whell3"/"whell4") only rolls.
        VehicleModel.TransformContext<Gaz66Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.5, 30f, "whell", "whell2");
        if (turn != null) return turn;

        VehicleModel.TransformContext<Gaz66Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.5, "whell3", "whell4");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
