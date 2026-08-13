package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.overlay.AmmoCountOverlay;
import com.atsuishio.superbwarfare.client.overlay.CommonOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hides SBW's ammo-pool overlay (Handgun / Rifle / Shotgun / Sniper / Heavy Ammo).
 *
 * <p>SBW draws that list across the middle of the screen whenever an AmmoSupplierItem is in the
 * main hand. FCP emplacements are fed by carrying ammo, so it is on screen exactly when the player
 * needs to see, and it obscures the view.
 *
 * <p>Targets {@link CommonOverlay}, NOT AmmoCountOverlay: shouldRender() is declared on the parent
 * and merely inherited by AmmoCountOverlay, and Mixin can only inject into methods present in the
 * targeted class's own bytecode - injecting on the subclass silently matches nothing. The instance
 * check keeps this limited to the ammo overlay so every other SBW overlay renders normally.
 *
 * <p>Purely visual: the ammo pool itself is untouched, so SBW's own weapons behave as before.
 */
@Mixin(value = CommonOverlay.class, remap = false)
public abstract class AmmoCountOverlayMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void fcp$hideAmmoPoolOverlay(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof AmmoCountOverlay) {
            cir.setReturnValue(false);
        }
    }
}