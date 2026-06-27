package frontline.combat.fcp.client.model.Aavp;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Aavp.AAVPEntity;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AAVPModel extends VehicleModel<AAVPEntity> {

    @Override
    public ResourceLocation getModelResource(AAVPEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/aavp.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
    
}