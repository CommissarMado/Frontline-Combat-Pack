package frontline.combat.fcp.client.model.Trailers.ExampleTrailer;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ExampleTrailerModel extends VehicleModel<ExampleTrailerEntity> {

    @Override
    public ResourceLocation getModelResource(ExampleTrailerEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/lav25.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
}
