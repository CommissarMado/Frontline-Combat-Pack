package frontline.combat.fcp.client.model.Emplacement;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplKornetEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EmplKornetModel extends FCPVehicleModel<EmplKornetEntity> {
    @Override public ResourceLocation getModelResource(EmplKornetEntity a) {return new ResourceLocation(FCP.MODID, "geo/empl_kornet.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<EmplKornetEntity> collectTransform(String boneName) {
        return super.collectTransform(boneName);
    }
}
