package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import frontline.combat.fcp.entity.vehicle.GroundContact;
import org.joml.Matrix4d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the ground-contact shift to SBW's tilted vehicle transforms, which everything posed on the
 * vehicle is built from (getVehicleTransform -> hitboxes, seats, shoot positions; the client camera).
 */
@Mixin(VehicleVecUtils.class)
public abstract class VehicleVecUtilsGroundMixin {

    @Inject(method = {"getVehicleYOffsetTransform", "getClientVehicleTransform"}, at = @At("RETURN"), cancellable = true, remap = false)
    private static void fcp$groundShift(VehicleEntity vehicle, float partialTicks, CallbackInfoReturnable<Matrix4d> cir) {
        if (!(vehicle instanceof GroundContact contact)) return;
        float shift = contact.fcp$groundOffset(partialTicks);
        if (shift != 0f) cir.setReturnValue(new Matrix4d().translation(0, shift, 0).mul(cir.getReturnValue()));
    }
}
