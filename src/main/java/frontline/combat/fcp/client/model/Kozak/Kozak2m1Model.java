package frontline.combat.fcp.client.model.Kozak;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Kozak.Kozak2m1Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Kozak2m1Model extends FCPVehicleModel<Kozak2m1Entity> {
    @Override public ResourceLocation getModelResource(Kozak2m1Entity animatable) {return new ResourceLocation(FCP.MODID, "geo/kozak2m1.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<Kozak2m1Entity> collectTransform(String boneName) {
        VehicleModel.TransformContext<Kozak2m1Entity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f, "whell", "whell2");
        if (steer != null) return steer;
        VehicleModel.TransformContext<Kozak2m1Entity> wheels = WheelRotationTransforms.matchAny(boneName, 0.6, "whell3", "whell4");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
