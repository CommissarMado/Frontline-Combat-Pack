package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.value.ReloadState;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private Player cachedGunner;
    private LazyOptional<IItemHandler> gunnerHandler = LazyOptional.empty();

    /**
     * Report the GUNNER's inventory as this emplacement's item handler.
     *
     * SBW resolves ammo — for counting, for the HUD's reserve figure, and for reloading — through
     * {@code entity.getCapability(ITEM_HANDLER)} on the VEHICLE. An emplacement has no container
     * and no hold, so that came back empty and everything downstream read zero: the HUD showed
     * "200 / 0" no matter what the player carried.
     *
     * Pointing the capability at the seated player makes every one of those paths work with no
     * changes to SBW at all — the vehicle IS the ammo supplier, it just happens to draw from the
     * crew. Note this is NOT the same as overriding ammoSupplier: that must stay the vehicle,
     * because VehicleGun.canShoot() starts with {@code if (shooter !is VehicleEntity) return false}.
     */
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        // Gate on usesInventoryReload(): this capability is what makes SBW resolve the vehicle's
        // ammo to the gunner's inventory, so leaving it on for the manual-loading emplacements
        // (ZiS-3, TOW, Kornet) let them keep drawing from the player through SBW's own paths even
        // after the reload cycle was disabled for them.
        if (cap == ForgeCapabilities.ITEM_HANDLER && usesInventoryReload()) {
            Player player = gunner();
            if (player != null) {
                if (player != cachedGunner || !gunnerHandler.isPresent()) {
                    cachedGunner = player;
                    final Player p = player;
                    gunnerHandler = LazyOptional.of(() -> new InvWrapper(p.getInventory()));
                }
                return gunnerHandler.cast();
            }
            cachedGunner = null;
            gunnerHandler = LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    /** The player manning this emplacement, or null. */
    protected Player gunner() {
        Entity controller = getNthEntity(getTurretControllerIndex());
        if (controller instanceof Player p) return p;
        if (getFirstPassenger() instanceof Player p) return p;

        // Client-side fallback. SBW tracks seats in its own orderedPassengers list, which is
        // populated on the SERVER; on the client it can be empty even while the player is visibly
        // riding. The HUD renders client-side, so without this the reserve figure resolves to no
        // gunner and reads 0. Vanilla's passenger list IS synced, so use it as the backstop.
        for (Entity passenger : getPassengers()) {
            if (passenger instanceof Player p) return p;
        }
        return null;
    }

    /**
     * Whether this emplacement is fed from the gunner's inventory at all.
     *
     * The ZiS-3, TOW and Kornet have their own manual loading systems (load a round by hand, then
     * fire) and must NOT pull ammo from the player automatically. They are excluded explicitly
     * rather than relying on their Magazine being 1, so retuning that value in the data files
     * cannot quietly switch inventory feeding back on for them.
     */
    protected boolean usesInventoryReload() {
        return true;
    }

    /** True for guns that use a magazine and so need this reload cycle at all. */
    protected boolean isMagazineFed(GunData data) {
        return usesInventoryReload()
                && data != null && !data.useBackpackAmmo() && data.get(GunProp.MAGAZINE) > 1;
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

        // A creative ammo box is a genuine unlimited supply: always fills, consumes nothing.
        boolean creativeBox =
                com.atsuishio.superbwarfare.tools.InventoryTool.hasCreativeAmmoBox(player);

        // Count the ACTUAL ammo items carried. countBackupAmmo() short-circuits to Int.MAX_VALUE
        // for a creative player, which is why creative used to reload from literally nothing -
        // countBackupAmmoItem() reports what is really in the inventory either way.
        int carried = gunData.countBackupAmmoItem(player);

        // Creative still needs to be HOLDING ammo to reload; it just isn't charged for it, so a
        // single stack fills the whole magazine instead of being capped at what it contains.
        boolean unlimited = creativeBox || (player.isCreative() && carried > 0);

        if (!unlimited && carried <= 0) return;

        int toReserve = unlimited ? needed : Math.min(needed, gunData.countBackupAmmo(player));
        if (toReserve <= 0) return;

        if (unlimited) {
            reservedAmmo = toReserve;                 // nothing consumed
        } else {
            int available = gunData.countBackupAmmo(player);
            modifyGunData(seatIndex, d -> d.consumeBackupAmmo(player, toReserve));

            // Only load what was ACTUALLY taken: a count that reports rounds consumeBackupAmmo()
            // then declines to remove would otherwise be a free magazine.
            int remaining = gunData.countBackupAmmo(player);
            int actuallyTaken = Math.max(0, available - remaining);
            if (actuallyTaken <= 0) {
                reservedAmmo = 0;
                return;
            }
            reservedAmmo = actuallyTaken;
        }

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