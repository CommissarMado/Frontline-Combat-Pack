package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.SafeDismount;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Every SBW vehicle: never put a dismounting or ejected passenger into or under the ground. */
@Mixin(VehicleEntity.class)
public abstract class VehicleDismountSafetyMixin {

    @Inject(method = "getDismountLocationForIndex", at = @At("RETURN"), cancellable = true, remap = false)
    private void fcp$safeDismount(LivingEntity passenger, int index, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(SafeDismount.vet((VehicleEntity) (Object) this, passenger, cir.getReturnValue(), true));
    }

    @Inject(method = "getEjectionPosition", at = @At("RETURN"), cancellable = true, remap = false)
    private void fcp$safeEjection(LivingEntity passenger, int index, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(SafeDismount.vet((VehicleEntity) (Object) this, passenger, cir.getReturnValue(), false));
    }
}
