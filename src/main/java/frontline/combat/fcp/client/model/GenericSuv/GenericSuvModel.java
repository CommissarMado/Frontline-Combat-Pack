package frontline.combat.fcp.client.model.GenericSuv;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.GenericSuv.GenericSuvEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GenericSuvModel extends VehicleModel<GenericSuvEntity> {

    @Override
    public ResourceLocation getModelResource(GenericSuvEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/generic_suv.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<GenericSuvEntity> collectTransform(String boneName) {
        // Front axle ("wheel1"/"wheel2") steers + rolls; rear axle ("wheel3"/"wheel4") only rolls.
        VehicleModel.TransformContext<GenericSuvEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.495, 30f, "wheel1", "wheel2");
        if (turn != null) return turn;

        VehicleModel.TransformContext<GenericSuvEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.495, "wheel3", "wheel4");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
