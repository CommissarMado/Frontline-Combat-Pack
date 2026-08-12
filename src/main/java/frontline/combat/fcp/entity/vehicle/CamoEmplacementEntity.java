package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.value.ReloadState;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base for crew-served emplacements (M2, DShK, MG3, Mk19, AGS-17, TOW, Kornet, ZiS-3).
 *
 * <p>Emplacements are unlike the rest of the pack: they have no cargo hold and no container
 * ({@code VehicleContainerType: Empty}), so their guns have no ammo source of their own - they are
 * fed by whoever is manning them. That needs a magazine reload cycle SBW does not provide for
 * vehicles (GunEventHandler only drives reloads for hand-held guns, {@code if (inMainHand)}), so it
 * is implemented here.
 *
 * <p>Deliberately a SEPARATE base rather than logic on {@link CamoVehicleBase}: every FCP vehicle
 * inherits that class, and none of this should apply to a vehicle that feeds from its own hold.
 *
 * <h2>Two constraints worth remembering</h2>
 * <ul>
 *   <li><b>Do not override {@code ammoSupplier}.</b> VehicleEntity.canShoot() passes it as the
 *       "shooter" into VehicleGun.canShoot(), which starts {@code if (shooter !is VehicleEntity)
 *       return false} - pointing it at the player makes the gun permanently unable to fire.</li>
 *   <li><b>Do not call {@code reloadAmmo()} to start a reload.</b> That is the INSTANT fill SBW
 *       performs at the END of a reload; calling it directly consumes the player's ammo with no
 *       reload time at all.</li>
 * </ul>
 *
 * <h2>How the reload works</h2>
 * Ammo is <em>reserved</em> (consumed from the gunner) when the reload STARTS, held while the
 * timer runs, then written into the magazine when it completes. Reserving up front is what makes
 * the ammo refundable: if the gunner dismounts mid-reload the reserved rounds go back into their
 * inventory rather than vanishing.
 */
public abstract class CamoEmplacementEntity extends CamoVehicleBase {

    /** Rounds taken from the gunner and held for the in-progress reload. */
    private int reservedAmmo = 0;
    /** Ticks remaining on the current reload, 0 when not reloading. */
    private int reloadTicks = 0;

    public CamoEmplacementEntity(EntityType<? extends VehicleEntity> type, Level world) {
        super(type, world);
    }

    /** The player manning this emplacement, or null. Public alias used by VehicleBackupAmmoMixin. */
    public Player fcp$gunner() {
        return gunner();
    }

    /** The player manning this emplacement, or null. */
    protected Player gunner() {
        Entity controller = getNthEntity(getTurretControllerIndex());
        if (controller instanceof Player p) return p;
        return getFirstPassenger() instanceof Player p ? p : null;
    }

    /** True for guns that use a magazine and so need this reload cycle at all. */
    protected boolean isMagazineFed(GunData data) {
        return data != null && !data.useBackpackAmmo() && data.get(GunProp.MAGAZINE) > 1;
    }

    /**
     * Drives the reload cycle. Call once per tick from the entity's tick().
     *
     * <p>Only ever runs while a player is aboard - an unmanned emplacement neither reloads nor
     * consumes anything.
     */
    protected void tickEmplacementReload(int seatIndex) {
        if (this.level().isClientSide()) return;

        GunData gunData = getGunData(seatIndex);
        if (!isMagazineFed(gunData)) return;

        Player player = gunner();

        // ---- reload in progress ----
        if (reloadTicks > 0) {
            if (player == null) {
                refundReservedAmmo(seatIndex);   // gunner left - give the rounds back
                cancelReload(seatIndex);
                return;
            }
            reloadTicks--;
            final int remaining = reloadTicks;
            modifyGunData(seatIndex, d -> d.reload.setTime(remaining));

            if (remaining <= 0) finishReload(seatIndex);
            return;
        }

        // ---- should a reload start? ----
        if (player == null) return;
        if (gunData.reloading()) return;
        if (gunData.ammo.get() > 0) return;                 // magazine still has rounds
        beginReload(seatIndex, gunData, player);
    }

    /** Reserves ammo from the gunner and starts the timer. */
    private void beginReload(int seatIndex, GunData gunData, Player player) {
        int needed = gunData.get(GunProp.MAGAZINE) - gunData.ammo.get();
        if (needed <= 0) return;

        // Creative (or a creative ammo box) is an unlimited supply: reserve a FULL magazine
        // rather than being capped by what happens to be in the stack. consumeBackupAmmo() is
        // already a no-op for creative, so nothing is actually taken.
        boolean unlimited = player.isCreative()
                || com.atsuishio.superbwarfare.tools.InventoryTool.hasCreativeAmmoBox(player);

        // countBackupAmmo() is READ-ONLY. Do NOT use AmmoConsumer.count(): for a PLAYER_AMMO
        // consumer that call converts the player's ammo ITEMS into their ammo pool as a side
        // effect, so merely inspecting the count consumes the stack.
        int available = gunData.countBackupAmmo(player);
        if (!unlimited && available <= 0) return;

        int toReserve = unlimited ? needed : Math.min(needed, available);
        // Take the rounds now so the reload is visible and refundable.
        modifyGunData(seatIndex, d -> d.consumeBackupAmmo(player, toReserve));
        reservedAmmo = toReserve;

        reloadTicks = Math.max(20, gunData.get(GunProp.EMPTY_RELOAD_TIME));
        final int total = reloadTicks;
        modifyGunData(seatIndex, d -> {
            d.reload.setState(ReloadState.EMPTY_RELOADING);
            d.reload.setTime(total);
        });
    }

    /** Writes the reserved rounds into the magazine. */
    private void finishReload(int seatIndex) {
        final int loaded = reservedAmmo;
        reservedAmmo = 0;
        reloadTicks = 0;
        modifyGunData(seatIndex, d -> {
            d.ammo.set(d.ammo.get() + loaded);
            d.reload.setTime(0);
            d.reload.setState(ReloadState.NOT_RELOADING);
        });
    }

    /** Returns reserved rounds to the gunner (or drops them if they've gone). */
    private void refundReservedAmmo(int seatIndex) {
        if (reservedAmmo <= 0) return;
        GunData gunData = getGunData(seatIndex);
        Player player = gunner();
        if (gunData != null && player != null) {
            final int refund = reservedAmmo;
            gunData.selectedAmmoConsumer().withdraw(player, refund);
        }
        reservedAmmo = 0;
    }

    private void cancelReload(int seatIndex) {
        reloadTicks = 0;
        modifyGunData(seatIndex, d -> {
            d.reload.setTime(0);
            d.reload.setState(ReloadState.NOT_RELOADING);
        });
    }


    /**
     * Manual reload request (the R key), routed here from ReloadEmplacementPacket.
     *
     * Tops the magazine up even when it is only partly empty, which the automatic cycle
     * deliberately does not do - that one only fires when the gun runs dry.
     */
    public void requestManualReload(Player player) {
        if (this.level().isClientSide()) return;
        if (gunner() != player) return;               // must actually be manning it
        if (reloadTicks > 0) return;                  // already reloading

        GunData gunData = getGunData(0);
        if (!isMagazineFed(gunData)) return;
        if (gunData.reloading()) return;
        if (gunData.ammo.get() >= gunData.get(GunProp.MAGAZINE)) return; // already full

        beginReload(0, gunData, player);
    }

    /** True while this emplacement is mid-reload (useful for models/HUD). */
    public boolean isReloading() {
        return reloadTicks > 0;
    }
}