package frontline.combat.fcp.client.model.Stryker;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMortarEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

// Extends VehicleModel (NOT FCPVehicleModel): the tube is aimed forward via the barrel bone's
// [0,180,0] base rotation, and FCPVehicleModel.resetSharedTransformBones() would zero that out
// (and flicker it with multiple mortars). The vanilla base leaves the bind pose intact, so the
// tube keeps its authored forward-at-0deg orientation and SBW's default barrel pitch matches the shot.
public class StrykerMortarModel extends VehicleModel<StrykerMortarEntity> {

    @Override
    public ResourceLocation getModelResource(StrykerMortarEntity a) {
        return new ResourceLocation(FCP.MODID, "geo/stryker_mortar.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<StrykerMortarEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<StrykerMortarEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "wheRR", "wheRR2", "wheRR3", "wheRR4", "wheRR5", "wheRR6", "wheRR7", "wheRR8");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
