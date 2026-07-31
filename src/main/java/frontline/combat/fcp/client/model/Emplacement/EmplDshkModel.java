package frontline.combat.fcp.client.model.Emplacement;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplDshkEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class EmplDshkModel extends FCPVehicleModel<EmplDshkEntity> {
    @Override public ResourceLocation getModelResource(EmplDshkEntity a) {return new ResourceLocation(FCP.MODID, "geo/empl_dshk.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<EmplDshkEntity> collectTransform(String boneName) {
        // Barrel-only emplacement: the barrel bone both rotates (yaw) and elevates (pitch).
        if ("barrel".equals(boneName)) {
            return (bone, vehicle, animationState) -> {
                float pt = (float) animationState.getPartialTick();
                float yaw = Mth.lerp(pt, vehicle.getTurretYRotO(), vehicle.getTurretYRot());
                float pitch = Mth.lerp(pt, vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                bone.setRotY(-yaw * Mth.DEG_TO_RAD);
                bone.setRotX(Mth.clamp(pitch, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
            };
        }
        return super.collectTransform(boneName);
    }
}
