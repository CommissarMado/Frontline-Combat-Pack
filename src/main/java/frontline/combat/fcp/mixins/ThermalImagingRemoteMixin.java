package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import frontline.combat.fcp.client.HimarsTvClient;
import frontline.combat.fcp.client.StugnaRemoteClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives a remote Stugna-P operator the launcher's thermal sight.
 *
 * <p>SBW gates thermal imaging on "wears thermal goggles OR sits in a thermal-capable vehicle seat":
 * its thermal key, the per-tick shader switch, the sky/fog colouring and the light-texture brightness
 * boost all check that. The operator stands at the control panel and rides nothing, so while they are
 * linked SBW is told they have goggles: its per-tick shader switch, sky/fog colouring and brightness
 * boost then keep thermal on, and once they disconnect the check fails again and SBW switches thermal
 * off itself. SBW's thermal KEY looks for goggles in the curio slots directly rather than through this
 * method, so StugnaRemoteClient toggles the thermal flag on that key itself.
 *
 * <p>The HIMARS TV-guided missile camera gets the same, but thermal is forced on there for the whole flight
 * (HimarsTvClient keeps the flag set).
 */
@Mixin(value = ClientEventHandler.class, remap = false)
public abstract class ThermalImagingRemoteMixin {

    @Inject(method = "hasThermalImagingGoggles", at = @At("HEAD"), cancellable = true)
    private static void fcp$stugnaThermalSight(CallbackInfoReturnable<Boolean> cir) {
        if (StugnaRemoteClient.grantsThermalSight() || HimarsTvClient.isActive()) {
            cir.setReturnValue(true);
        }
    }
}
