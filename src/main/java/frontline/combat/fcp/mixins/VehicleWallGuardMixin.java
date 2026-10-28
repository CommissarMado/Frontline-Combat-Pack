package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.WallGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Collision-box vehicles are pushed back out of walls they turned into (see WallGuard). */
@Mixin(VehicleEntity.class)
public abstract class VehicleWallGuardMixin {

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void fcp$pushOutOfWalls(CallbackInfo ci) {
        WallGuard.pushOutOfWalls((VehicleEntity) (Object) this);
    }
}
