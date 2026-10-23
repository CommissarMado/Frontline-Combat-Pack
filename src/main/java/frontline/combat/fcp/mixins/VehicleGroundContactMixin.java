package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.GroundContact;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Every SBW vehicle tracks a ground-contact shift (non-zero only for landed helicopters, see GroundContact). */
@Mixin(VehicleEntity.class)
public abstract class VehicleGroundContactMixin implements GroundContact {

    @Unique
    private float fcp$groundShift, fcp$groundShiftO;

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void fcp$updateGroundShift(CallbackInfo ci) {
        fcp$groundShiftO = fcp$groundShift;
        float target = GroundContact.solve((VehicleEntity) (Object) this);
        // Ease in so take-off/touch-down and tilt changes don't pop.
        fcp$groundShift += (target - fcp$groundShift) * 0.4f;
        if (Math.abs(fcp$groundShift) < 1.0e-4f) fcp$groundShift = 0f;
    }

    /** Landed helicopters: our geometric settle replaces SBW's terrain tilt. */
    @Inject(method = "terrainCompact", at = @At("HEAD"), cancellable = true, remap = false)
    private void fcp$settleOnGear(List<Vec3> positions, CallbackInfo ci) {
        if (GroundContact.settle((VehicleEntity) (Object) this)) ci.cancel();
    }

    @Override
    public float fcp$groundOffset(float partialTick) {
        return Mth.lerp(partialTick, fcp$groundShiftO, fcp$groundShift);
    }
}
