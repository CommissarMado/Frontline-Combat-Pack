package frontline.combat.fcp.client.model.Stryker;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMortarEntity;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class StrykerMortarModel extends FCPVehicleModel<StrykerMortarEntity> {

    @Override public ResourceLocation getModelResource(StrykerMortarEntity a) { return new ResourceLocation(FCP.MODID, "geo/stryker_mortar.geo.json"); }
    @Override public boolean hideForTurretControllerWhileZooming() { return false; }

    @Override public @Nullable VehicleModel.TransformContext<StrykerMortarEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<StrykerMortarEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "wheRR", "wheRR2", "wheRR3", "wheRR4", "wheRR5", "wheRR6", "wheRR7", "wheRR8");
        if (wheels != null) return wheels;

        // M249 secondary MG: hull-independent of the 360 mortar turret. Driven by the
        // passenger weapon station gunner (gunYRot/gunXRot), clamped to a 180-deg forward
        // arc. No -turretYRot term (SBW's passengerWeaponStationYaw assumes a turret-nested
        // station) because m249gun is parented to the hull, so its aim is already hull-relative.
        if ("m249gun".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float yaw = Mth.lerp((float) state.getPartialTick(), vehicle.getGunYRotO(), vehicle.getGunYRot());
                float pitch = Mth.lerp((float) state.getPartialTick(), vehicle.getGunXRotO(), vehicle.getGunXRot());
                bone.setRotY(Mth.clamp(yaw, -90f, 90f) * Mth.DEG_TO_RAD);
                bone.setRotX(Mth.clamp(-pitch, -10f, 20f) * Mth.DEG_TO_RAD);
            };
        }

        return super.collectTransform(boneName);
    }
}
