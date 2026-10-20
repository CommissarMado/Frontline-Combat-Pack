package frontline.combat.fcp.client.model.TyphoonVdv;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.TyphoonVdv.TyphoonVdvEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class TyphoonVdvModel extends VehicleModel<TyphoonVdvEntity> {

    @Override
    public ResourceLocation getModelResource(TyphoonVdvEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/typhoon_vdv.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<TyphoonVdvEntity> collectTransform(String boneName) {
        // Front axle ("whell"/"whell2") steers + rolls; rear axle ("whell3"/"whell4") only rolls.
        // Turret/barrel rotation is handled automatically by the base VehicleModel via the
        // vehicle JSON's "Transform": "Turret" OBB entry - no custom code needed here.
        VehicleModel.TransformContext<TyphoonVdvEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.643, 35f, "whell", "whell2");
        if (turn != null) return turn;

        VehicleModel.TransformContext<TyphoonVdvEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.643, "whell3", "whell4");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
