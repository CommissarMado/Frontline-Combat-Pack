package frontline.combat.fcp.client.model.TyphoonVdvUnarmed;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.TyphoonVdvUnarmed.TyphoonVdvUnarmedEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class TyphoonVdvUnarmedModel extends VehicleModel<TyphoonVdvUnarmedEntity> {

    @Override
    public ResourceLocation getModelResource(TyphoonVdvUnarmedEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/typhoon_vdv_unarmed.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<TyphoonVdvUnarmedEntity> collectTransform(String boneName) {
        // Front axle ("whell"/"whell2") steers + rolls; rear axle ("whell3"/"whell4") only rolls.
        VehicleModel.TransformContext<TyphoonVdvUnarmedEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.643, 35f, "whell", "whell2");
        if (turn != null) return turn;

        VehicleModel.TransformContext<TyphoonVdvUnarmedEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.643, "whell3", "whell4");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
