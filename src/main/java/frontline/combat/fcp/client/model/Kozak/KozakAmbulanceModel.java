package frontline.combat.fcp.client.model.Kozak;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Kozak.KozakAmbulanceEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class KozakAmbulanceModel extends FCPVehicleModel<KozakAmbulanceEntity> {
    @Override public ResourceLocation getModelResource(KozakAmbulanceEntity animatable) {return new ResourceLocation(FCP.MODID, "geo/kozak_ambulance.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<KozakAmbulanceEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<KozakAmbulanceEntity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f, "whell", "whell2");
        if (steer != null) return steer;
        VehicleModel.TransformContext<KozakAmbulanceEntity> wheels = WheelRotationTransforms.matchAny(boneName, 0.6, "whell3", "whell4");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
