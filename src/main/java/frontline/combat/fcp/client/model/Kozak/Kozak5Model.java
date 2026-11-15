package frontline.combat.fcp.client.model.Kozak;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Kozak.Kozak5Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Kozak5Model extends FCPVehicleModel<Kozak5Entity> {
    // kozak5 (PKM) or kozak5_ags (AGS-17): geo by registry id
    @Override public ResourceLocation getModelResource(Kozak5Entity animatable) {return new ResourceLocation(FCP.MODID, "geo/" + net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(animatable.getType()).getPath() + ".geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<Kozak5Entity> collectTransform(String boneName) {
        VehicleModel.TransformContext<Kozak5Entity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.43, 30f, "whell", "whell2");
        if (steer != null) return steer;
        VehicleModel.TransformContext<Kozak5Entity> wheels = WheelRotationTransforms.matchAny(boneName, 0.43, "whell3", "whell4");
        if (wheels != null) return wheels;
        return super.collectTransform(boneName);
    }
}
