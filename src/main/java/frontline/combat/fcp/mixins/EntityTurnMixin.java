package frontline.combat.fcp.mixins;

import frontline.combat.fcp.client.HimarsTvClient;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client: while the local player flies a HIMARS TV missile, mouse look steers the missile instead of turning the
 * player (whose gunner seat would otherwise clamp it and turn the launcher).
 */
@Mixin(Entity.class)
public abstract class EntityTurnMixin {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void fcp$steerTvMissile(double yRot, double xRot, CallbackInfo ci) {
        if (HimarsTvClient.consumeTurn((Entity) (Object) this, yRot, xRot)) ci.cancel();
    }
}
