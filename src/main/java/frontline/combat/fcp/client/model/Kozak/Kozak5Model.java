package frontline.combat.fcp.client.model.Kozak;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Kozak.Kozak5Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Kozak5Model extends FCPVehicleModel<Kozak5Entity> {
    @Override public ResourceLocation getModelResource(Kozak5Entity animatable) {return new ResourceLocation(FCP.MODID, "geo/kozak5.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<Kozak5Entity> collectTransform(String boneName) {
        VehicleModel.TransformContext<Kozak5Entity> steer = WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f, "whell", "whell2");
        if (steer != null) return steer;
        VehicleModel.TransformContext<Kozak5Entity> wheels = WheelRotationTransforms.matchAny(boneName, 0.6, "whell3", "whell4");
        if (wheels != null) return wheels;
        if ("barrel".equals(boneName)) {
            // The turret subtree is flipped 180 by the Thehatch bone, which inverts the
            // barrel pitch. Flip the sign so the gun elevates the correct way.
            return (bone, vehicle, animationState) -> {
                float xRot = Mth.lerp(animationState.getPartialTick(), vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                bone.setRotX(Mth.clamp(xRot, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
            };
        }
        return super.collectTransform(boneName);
    }
}
