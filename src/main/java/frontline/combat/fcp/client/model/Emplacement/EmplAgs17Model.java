package frontline.combat.fcp.client.model.Emplacement;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplAgs17Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EmplAgs17Model extends FCPVehicleModel<EmplAgs17Entity> {
    @Override public ResourceLocation getModelResource(EmplAgs17Entity a) {return new ResourceLocation(FCP.MODID, "geo/empl_ags17.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<EmplAgs17Entity> collectTransform(String boneName) {
        // barrel-only emplacement: the barrel bone both rotates (yaw) and elevates (pitch).
        // NOTE: signs mirror the (post-flip) DShK; if the model is flipped they may need inverting.
        if ("barrel".equals(boneName)) {
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
