package frontline.combat.fcp.client.model.Trailers.ExampleTrailer;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import net.minecraft.resources.ResourceLocation;

public class ExampleTrailerModel extends VehicleModel<ExampleTrailerEntity> {

    @Override
    public ResourceLocation getModelResource(ExampleTrailerEntity animatable) {
        // Placeholder geo — swap for your trailer's own model.
        return new ResourceLocation(FCP.MODID, "geo/lav25.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
}