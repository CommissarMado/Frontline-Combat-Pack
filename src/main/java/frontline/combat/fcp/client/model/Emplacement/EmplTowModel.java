package frontline.combat.fcp.client.model.Emplacement;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplTowEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EmplTowModel extends FCPVehicleModel<EmplTowEntity> {
    @Override public ResourceLocation getModelResource(EmplTowEntity a) {return new ResourceLocation(FCP.MODID, "geo/empl_tow.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<EmplTowEntity> collectTransform(String boneName) {
        if ("Magazine".equals(boneName)) {
            return (bone, vehicle, animationState) -> bone.setHidden(!vehicle.isLoaded());
        }
        return super.collectTransform(boneName);
    }
}
