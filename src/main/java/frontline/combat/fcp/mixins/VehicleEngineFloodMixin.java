package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.EngineFlooding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Land vehicles that can't float flood their engine underwater (see EngineFlooding). */
@Mixin(VehicleEntity.class)
public abstract class VehicleEngineFloodMixin {

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void fcp$floodEngine(CallbackInfo ci) {
        EngineFlooding.tick((VehicleEntity) (Object) this);
    }
}
