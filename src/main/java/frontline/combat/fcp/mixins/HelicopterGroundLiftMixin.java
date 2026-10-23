package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEngineUtils;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Landed helicopters lift straight up. SBW pushes a helicopter along its tilted up-vector every tick,
 * so a helicopter resting tilted on its gear (see GroundContact) slid off in the tilt's direction while
 * idling. On the ground the tilt is only how the airframe sits on its wheels/skids, so the lift there is
 * vertical; once airborne the normal tilted lift (and with it all flight control) applies again.
 */
@Mixin(VehicleEngineUtils.class)
public abstract class HelicopterGroundLiftMixin {

    @Redirect(method = "helicopterEngine", remap = false, at = @At(value = "INVOKE", remap = false,
            target = "Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;getUpVec(F)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 fcp$verticalLiftWhenLanded(VehicleEntity vehicle, float partialTicks) {
        return vehicle.onGround() ? new Vec3(0, 1, 0) : vehicle.getUpVec(partialTicks);
    }
}
