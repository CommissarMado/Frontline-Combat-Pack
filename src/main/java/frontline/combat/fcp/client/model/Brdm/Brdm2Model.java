package frontline.combat.fcp.client.model.Brdm;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Brdm.Brdm2Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Brdm2Model extends FCPVehicleModel<Brdm2Entity> {
    @Override public ResourceLocation getModelResource(Brdm2Entity a) {return new ResourceLocation(FCP.MODID, "geo/brdm2.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<Brdm2Entity> collectTransform(String boneName) {
        VehicleModel.TransformContext<Brdm2Entity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f, "WheelTurnR2", "WheelTurnR3");
        if (steer != null) return steer;
        VehicleModel.TransformContext<Brdm2Entity> wheels = WheelRotationTransforms.matchAny(boneName, 0.6, "WheelTurnR1", "WheelTurnR4");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
