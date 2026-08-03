package frontline.combat.fcp.client.model.Ural;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Ural.UralFuelEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class UralFuelModel extends FCPVehicleModel<UralFuelEntity> {
    @Override public ResourceLocation getModelResource(UralFuelEntity a) {return new ResourceLocation(FCP.MODID, "geo/ural_fuel.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<UralFuelEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<UralFuelEntity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f, "whell", "whell2");
        if (steer != null) return steer;
        VehicleModel.TransformContext<UralFuelEntity> wheels = WheelRotationTransforms.matchAny(boneName, 0.6, "whell3", "whell4", "whell5", "whell6");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
