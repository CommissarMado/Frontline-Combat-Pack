package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.overlay.AmmoCountOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Suppresses SBW's "hold ammo to see your ammo pool" overlay.
 *
 * <p>SBW draws a large list (Handgun / Rifle / Shotgun / Sniper / Heavy Ammo) across the middle of
 * the screen whenever an AmmoSupplierItem is in the main hand. FCP emplacements are fed by
 * carrying ammo, so that overlay is on screen almost exactly when the player needs to see, and it
 * obscures the view.
 *
 * <p>Forces shouldRender() to false, which CommonOverlay.render() checks before drawing anything.
 * Purely visual: the ammo pool itself is untouched, so SBW's own weapons behave as before.
 */
@Mixin(value = AmmoCountOverlay.class, remap = false)
public abstract class AmmoCountOverlayMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void fcp$hideAmmoPoolOverlay(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}