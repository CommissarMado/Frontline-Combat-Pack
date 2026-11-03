package frontline.combat.fcp.client.model.Brdm;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Brdm.Brdm2Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Brdm2Model extends FCPVehicleModel<Brdm2Entity> {
    /** "brdm2" or "brdm2_kornet" (same entity class, registry name picks the geo). */
    @Override public ResourceLocation getModelResource(Brdm2Entity a) {return new ResourceLocation(FCP.MODID, "geo/" + net.minecraft.world.entity.EntityType.getKey(a.getType()).getPath() + ".geo.json");}

    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<Brdm2Entity> collectTransform(String boneName) {
        VehicleModel.TransformContext<Brdm2Entity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.52, 30f, "WheelTurnR2", "WheelTurnR3");
        if (steer != null) return steer;
        VehicleModel.TransformContext<Brdm2Entity> wheels = WheelRotationTransforms.matchAny(boneName, 0.52, "WheelTurnR1", "WheelTurnR4");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
