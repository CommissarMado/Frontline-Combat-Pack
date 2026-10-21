package frontline.combat.fcp.client.model.Stugna;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Stugna.StugnaLauncherEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class StugnaLauncherModel extends FCPVehicleModel<StugnaLauncherEntity> {
    @Override public ResourceLocation getModelResource(StugnaLauncherEntity a) {return new ResourceLocation(FCP.MODID, "geo/stugna_launcher.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<StugnaLauncherEntity> collectTransform(String boneName) {
        // The transport-launch container (tube + missile) is only shown while loaded.
        if ("missile".equals(boneName)) {
            return (bone, vehicle, animationState) -> bone.setHidden(!vehicle.isLoaded());
        }
        return super.collectTransform(boneName);
    }
}
