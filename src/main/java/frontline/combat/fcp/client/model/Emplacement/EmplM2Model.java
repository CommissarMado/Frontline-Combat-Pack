package frontline.combat.fcp.client.model.Emplacement;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplM2Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EmplM2Model extends FCPVehicleModel<EmplM2Entity> {
    @Override public ResourceLocation getModelResource(EmplM2Entity a) {return new ResourceLocation(FCP.MODID, "geo/empl_m2.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<EmplM2Entity> collectTransform(String boneName) {
        // turret-only emplacement: the turret bone both rotates (yaw) and elevates (pitch).
        // NOTE: signs mirror the (post-flip) DShK; if the model is flipped they may need inverting.
        if ("turret".equals(boneName)) {
            return (bone, vehicle, animationState) -> {
                float pt = (float) animationState.getPartialTick();
                float yaw = net.minecraft.util.Mth.lerp(pt, vehicle.getTurretYRotO(), vehicle.getTurretYRot());
                float pitch = net.minecraft.util.Mth.lerp(pt, vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                bone.setRotY(yaw * net.minecraft.util.Mth.DEG_TO_RAD);
                bone.setRotX(-net.minecraft.util.Mth.clamp(pitch, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * net.minecraft.util.Mth.DEG_TO_RAD);
            };
        }
        return super.collectTransform(boneName);
    }
}
