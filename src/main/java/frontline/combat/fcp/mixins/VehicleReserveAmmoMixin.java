package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.VehicleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The reserve shown on the weapon HUD. An FCP hold isn't synced to clients (only its open menu is),
 * so the client can't count it; SBW does sync GunData.backupAmmoCount, but zeroes it for
 * magazine-fed guns. For FCP vehicles every seated gun gets its hold count published there instead,
 * and VehicleHudMagazineMixin shows that.
 */
@Mixin(value = VehicleEntity.class, remap = false)
public abstract class VehicleReserveAmmoMixin {

    @Inject(method = "updateBackupAmmoCount", at = @At("HEAD"), cancellable = true)
    private void fcp$publishHoldReserve(CallbackInfo ci) {
        if (!(this instanceof VehicleInventory)) return;
        VehicleEntity self = (VehicleEntity) (Object) this;
        for (int seat = 0; seat < self.getMaxPassengers(); seat++) {
            GunData data = self.getGunData(seat);
            if (data == null) continue;
            data.cachedBackupAmmo = -1;
            int count = data.countBackupAmmo(self.getAmmoSupplier());
            if (data.backupAmmoCount.get() != count) self.modifyGunData(seat, d -> d.backupAmmoCount.set(count));
        }
        ci.cancel();
    }
}
