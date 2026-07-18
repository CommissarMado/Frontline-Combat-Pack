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

    @Override
    public ResourceLocation getModelResource(StrykerMortarEntity a) {
        return new ResourceLocation(FCP.MODID, "geo/stryker_mortar.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<StrykerMortarEntity> collectTransform(String boneName) {

        VehicleModel.TransformContext<StrykerMortarEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "wheRR", "wheRR2", "wheRR3", "wheRR4", "wheRR5", "wheRR6", "wheRR7", "wheRR8");
        if (wheels != null) return wheels;

        // Mortar tube elevation. The tube geometry points UP (+Y) at rest, but SBW's shoot
        // (getBarrelTransform) assumes the barrel points FORWARD (+Z) and pitches by turretXRot.
        // That 90-deg offset is why the tube didn't match the shot. Subtract 90 so the tube
        // starts forward (matching +Z) and carries the same pitch the shot uses. If the tube
        // ends up pointing the wrong way, flip to (90 - pitch).
        if ("barrel".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float xr = Mth.lerp((float) state.getPartialTick(), vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                float pitch = Mth.clamp(-xr, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch());
                bone.setRotX((pitch - 90f) * Mth.DEG_TO_RAD);
            };
        }

        // M249 secondary MG. turret2 = hull-parented yaw mount, barrel2 = its pitch child.
        // NOTE: SBW's passenger weapon station is architecturally built on the turret
        // (getGunTransform starts from getTurretTransform), so the station's aim value (gunYRot)
        // is turret-referenced and its shoot position orbits with the mortar turret. Making the
        // M249 fully hull-independent needs a mixin that rebuilds getGunTransform on the hull; this
        // transform only decouples the visual as far as the data allows.
        if ("turret2".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float pt = (float) state.getPartialTick();
                float yaw = Mth.lerp(pt, vehicle.getGunYRotO(), vehicle.getGunYRot());
                float turretYaw = Mth.lerp(pt, vehicle.getTurretYRotO(), vehicle.getTurretYRot());
                bone.setRotY(Mth.clamp(yaw - turretYaw, -90f, 90f) * Mth.DEG_TO_RAD);
            };
        }
        if ("barrel2".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float pitch = Mth.lerp((float) state.getPartialTick(), vehicle.getGunXRotO(), vehicle.getGunXRot());
                bone.setRotX(Mth.clamp(-pitch, -10f, 20f) * Mth.DEG_TO_RAD);
            };
        }

        return super.collectTransform(boneName);
    }
}
