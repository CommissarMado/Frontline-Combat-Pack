package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * No first-person fire sound from a lock-only missile (CamoVehicleBase.lockOnlyWeapons: the Viper's Sidewinder, the
 * Ka-52's Igla) with nothing locked. SBW plays the 1P sound on the client as soon as it sends the fire packet, but the
 * server refuses the launch without a locked target (CamoVehicleBase.refuseUnlocked), so the player heard a shot that
 * never left the rail. SBW only sets lockingEntityVehicle once the lock is complete - the target the packet carries.
 */
@Mixin(value = ClientEventHandler.class, remap = false)
public abstract class VehicleLockFireSoundMixin {

    @Inject(method = "playVehicleClientSounds", at = @At("HEAD"), cancellable = true)
    private void fcp$noSoundWithoutLock(Player player, VehicleEntity vehicle, CallbackInfo ci) {
        if (ClientEventHandler.lockingEntityVehicle == null && vehicle instanceof CamoVehicleBase camo
                && camo.lockOnly(vehicle.getSeatIndex(player))) {
            ci.cancel();
        }
    }
}
