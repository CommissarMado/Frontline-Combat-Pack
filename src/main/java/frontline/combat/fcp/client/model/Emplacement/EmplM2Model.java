package frontline.combat.fcp.client.model.Emplacement;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplM2Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class EmplM2Model extends FCPVehicleModel<EmplM2Entity> {
    @Override public ResourceLocation getModelResource(EmplM2Entity a) {return ResourceLocation.fromNamespaceAndPath(FCP.MODID, "geo/empl_m2.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<EmplM2Entity> collectTransform(String boneName) {
        // Two-bone split (bones renamed to the turret/barrel convention):
        //   turret -> yaw only (traverses the whole mount top)
        //   barrel -> pitch only (elevates the gun around the barrel trunnion; gun body is its child)
        // NOTE: pitch sign mirrors the (post-flip) DShK; if elevation is inverted in-game, flip the -.
        if ("turret".equals(boneName)) {
            return (bone, vehicle, animationState) -> {
                float pt = (float) animationState.getPartialTick();
                float yaw = Mth.lerp(pt, vehicle.getTurretYRotO(), vehicle.getTurretYRot());
                bone.setRotY(yaw * Mth.DEG_TO_RAD);
            };
        }
        if ("barrel".equals(boneName)) {
            return (bone, vehicle, animationState) -> {
                float pt = (float) animationState.getPartialTick();
                float pitch = Mth.lerp(pt, vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                bone.setRotX(-Mth.clamp(pitch, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
            };
        }
        return super.collectTransform(boneName);
    }
}
