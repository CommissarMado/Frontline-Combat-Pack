package frontline.combat.fcp.client.model.Pantsir;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Pantsir.PantsirEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class PantsirModel extends FCPVehicleModel<PantsirEntity> {
    @Override public ResourceLocation getModelResource(PantsirEntity a) {return new ResourceLocation(FCP.MODID, "geo/pantsir_s1.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<PantsirEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<PantsirEntity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.494, 30f, "whell2", "whell6", "whell7", "whell8");
        if (steer != null) return steer;
        VehicleModel.TransformContext<PantsirEntity> wheels = WheelRotationTransforms.matchAny(boneName, 0.494, "whell3", "whell9", "whell4", "whell5");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
