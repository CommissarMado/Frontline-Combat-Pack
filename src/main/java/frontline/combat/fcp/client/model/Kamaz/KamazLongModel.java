package frontline.combat.fcp.client.model.Kamaz;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazLongEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class KamazLongModel extends FCPVehicleModel<KamazLongEntity> {
    @Override public ResourceLocation getModelResource(KamazLongEntity a) {return new ResourceLocation(FCP.MODID, "geo/kamaz_long.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<KamazLongEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<KamazLongEntity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f, "whell2", "whell6");
        if (steer != null) return steer;
        VehicleModel.TransformContext<KamazLongEntity> wheels = WheelRotationTransforms.matchAny(boneName, 0.6, "whell7", "whell8", "whell3", "whell9", "whell4", "whell5");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
