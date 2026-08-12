package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.CamoEmplacementEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes crew-served emplacements show the gunner's carried rounds as the HUD reserve figure.
 *
 * <p>SBW's {@code updateBackupAmmoCount()} zeroes {@code backupAmmoCount} for any magazine-fed gun
 * (its reserve is meant to be "rounds available beyond the magazine", which for a normal vehicle is
 * always zero). That is why FCP emplacements read "200 / 0".
 *
 * <p>An emplacement has no hold of its own — it is fed by whoever is manning it — so the reserve
 * that belongs there is the gunner's own carried ammo. The method is a plain (final) Kotlin
 * function, so it cannot be overridden; and writing the value from a tick loses a race with SBW's
 * own update. Injecting at RETURN means we always write last.
 *
 * <p>Only touches {@link CamoEmplacementEntity}: every other vehicle keeps stock behaviour.
 */
@Mixin(value = VehicleEntity.class, remap = false)
public abstract class VehicleBackupAmmoMixin {

    @Inject(method = "updateBackupAmmoCount", at = @At("RETURN"))
    private void fcp$showGunnerAmmoForEmplacements(CallbackInfo ci) {
        if (!(((Object) this) instanceof CamoEmplacementEntity emplacement)) return;
        if (emplacement.level().isClientSide()) return;

        Player gunner = emplacement.fcp$gunner();
        if (gunner == null) return;

        for (int i = 0; i < emplacement.getMaxPassengers(); i++) {
            GunData data = emplacement.getGunData(i);
            if (data == null) continue;
            // Only magazine-fed guns were zeroed; backpack-ammo guns are already correct.
            if (data.useBackpackAmmo() || data.get(GunProp.MAGAZINE) <= 1) continue;

            // countBackupAmmo() is read-only. NEVER use AmmoConsumer.count() here: for a
            // PLAYER_AMMO consumer that converts the player's ammo items into their ammo pool as
            // a side effect, so merely reading the count would consume the stack every tick.
            int carried = data.countBackupAmmo(gunner);
            if (data.backupAmmoCount.get() != carried) {
                final int c = carried;
                emplacement.modifyGunData(i, d -> d.backupAmmoCount.set(c));
            }
        }
    }
}