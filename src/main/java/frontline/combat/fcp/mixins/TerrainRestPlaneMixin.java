package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleMotionUtils;
import frontline.combat.fcp.entity.vehicle.RigidTerrainRest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Collision-box vehicles: SBW's terrainCompact fits an average slope and hands it to
 * updateTerrainCompact. Swap that one call for the rigid-hull rest attitude (RigidTerrainRest), so a
 * hull perched on a block tips off it instead of balancing level in mid-air. SBW's gating, sampling
 * cut-offs and dust particles around the call are untouched.
 */
@Mixin(value = VehicleMotionUtils.class, remap = false)
public abstract class TerrainRestPlaneMixin {

    @Redirect(method = "terrainCompact", remap = false,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lcom/atsuishio/superbwarfare/entity/vehicle/utils/VehicleMotionUtils;updateTerrainCompact(Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;DDDD)V"))
    private static void fcp$restOnTerrain(VehicleEntity vehicle, double sumXH, double sumZH, double sumX2, double sumZ2) {
        RigidTerrainRest.apply(vehicle);
    }
}
