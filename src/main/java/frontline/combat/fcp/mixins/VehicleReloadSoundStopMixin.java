package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.ReloadSoundStopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every SBW vehicle: stop a weapon's reload sound when its ammo type is switched mid-reload. */
@Mixin(VehicleEntity.class)
public abstract class VehicleReloadSoundStopMixin {

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void fcp$stopReloadSoundOnAmmoSwitch(CallbackInfo ci) {
        ReloadSoundStopper.tick((VehicleEntity) (Object) this);
    }
}
