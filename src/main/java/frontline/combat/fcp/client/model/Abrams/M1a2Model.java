package frontline.combat.fcp.client.model.Abrams;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a2Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class M1a2Model extends FCPVehicleModel<M1a2Entity> {

    @Override
    public ResourceLocation getModelResource(M1a2Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/m1a1.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    protected FCPTrackPath getTrackPath() {
        return M1a1TrackPaths.M1A1.reversed();
    }
}
