package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import frontline.combat.fcp.FcpWorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scales an artillery main gun's GRAVITY by the F8 range multipliers (FcpWorldSettings). Done on the
 * property itself so every reader - the shot, the HUD solution, the artillery indicator - sees it.
 */
@Mixin(value = GunData.class, remap = false)
public abstract class ArtilleryGravityMixin {

    @Inject(method = "get(Lcom/atsuishio/superbwarfare/data/gun/GunProp;)Ljava/lang/Object;", at = @At("RETURN"), cancellable = true)
    private void fcp$artilleryGravity(GunProp<?, ?> prop, CallbackInfoReturnable<Object> cir) {
        if (prop != GunProp.GRAVITY || !(cir.getReturnValue() instanceof Double gravity)) return;
        double multiplier = FcpWorldSettings.gravityMultiplier((GunData) (Object) this);
        if (multiplier != 1.0) cir.setReturnValue(gravity * multiplier);
    }
}
