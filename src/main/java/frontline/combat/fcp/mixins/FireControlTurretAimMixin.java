package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.IndirectFireVehicleBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * While fire control is active, suppress Superb Warfare's player-look turret adjust.
 * {@link VehicleEntity#baseTick} always aims the barrel toward the gunner's view when a
 * player is in the turret seat; without this cancel, that path fights
 * {@link IndirectFireVehicleBase}'s per-tick {@code turretAutoAimFromVector(solution)}
 * and the weapon never converges on the firing solution.
 */
@Mixin(value = VehicleEntity.class, remap = false)
public abstract class FireControlTurretAimMixin {

    @Inject(method = "adjustTurretAngle", at = @At("HEAD"), cancellable = true)
    private void fcp$skipLookAimWhenFireControlActive(CallbackInfo ci) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        if (self instanceof IndirectFireVehicleBase vehicle && vehicle.isFireControlActive()) {
            ci.cancel();
        }
    }
}
