package frontline.combat.fcp.client.model.Bmp.Bmp1;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Bmp.BmpTrackPaths;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp1.BMP1Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BMP1Model extends FCPVehicleModel<BMP1Entity> {

    @Override
    public ResourceLocation getModelResource(BMP1Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/bmp1.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<BMP1Entity> collectTransform(String boneName) {
        // Hide the Malyutka missile once it has been fired.
        if ("Malyutka".equals(boneName)) {
            return (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("Malyutka", 0));
        }

        return super.collectTransform(boneName);
    }

    @Override
    protected FCPTrackPath getTrackPath() {

        return BmpTrackPaths.BMP.reversed();
    }
}
