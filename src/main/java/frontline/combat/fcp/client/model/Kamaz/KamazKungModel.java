package frontline.combat.fcp.client.model.Kamaz;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazKungEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class KamazKungModel extends FCPVehicleModel<KamazKungEntity> {
    @Override public ResourceLocation getModelResource(KamazKungEntity a) {return new ResourceLocation(FCP.MODID, "geo/kamaz_kung.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<KamazKungEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<KamazKungEntity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.494, 30f, "whell", "whell4");
        if (steer != null) return steer;
        VehicleModel.TransformContext<KamazKungEntity> wheels = WheelRotationTransforms.matchAny(boneName, 0.494, "whell2", "whell5", "whell3", "whell6");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
