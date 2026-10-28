package frontline.combat.fcp.balance;

import frontline.combat.fcp.FcpWorldSettings;

import com.atsuishio.superbwarfare.block.entity.ChargingStationBlockEntity;
import com.atsuishio.superbwarfare.data.gun.Ammo;
import com.atsuishio.superbwarfare.data.gun.AmmoConsumer;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.ammo.AmmoBoxItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Squad-style vehicle rearming (used by the rearm station): every {@link #TICKS_PER_MAG} ticks in range,
 * each weapon short of its Squad loadout gets one more magazine in the vehicle's inventory. Rounds
 * already loaded in a weapon count toward the loadout, and weapons that share an ammo item (a coax and
 * a pintle MG both on rifle ammo) are filled as one pool.
 */
public final class VehicleRearm {

    /** Squad's repair station: SQVehicleDepotComponent.SecondsPerMag = 8.5 s. */
    public static final int TICKS_PER_MAG = 170;
    private static final String PROGRESS = "fcp_rearm", LAST_TICK = "fcp_rearm_tick", BORN = "fcp_born";

    private VehicleRearm() {
    }

    /**
     * Called by a station every {@code ticks} ticks for a vehicle in range. Several stations covering
     * the same vehicle don't stack: only the first to run in a given game tick advances it.
     */
    public static void service(VehicleEntity vehicle, int ticks) {
        CompoundTag data = vehicle.getPersistentData();
        long now = vehicle.level().getGameTime();
        if (data.getLong(LAST_TICK) == now) return;
        data.putLong(LAST_TICK, now);

        recharge(vehicle, ticks);

        int progress = data.getInt(PROGRESS) + ticks;
        if (progress >= TICKS_PER_MAG) {
            progress -= TICKS_PER_MAG;
            rearm(vehicle, false);
        }
        data.putInt(PROGRESS, progress);
    }

    /** Same rate as SBW's charging station gives entities around it. */
    private static void recharge(VehicleEntity vehicle, int ticks) {
        vehicle.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            if (energy.canReceive()) energy.receiveEnergy(ChargingStationBlockEntity.CHARGE_OTHER_SPEED * ticks, false);
        });
    }

    /** One ammo item's share of the loadout across every weapon that feeds from it. */
    private static final class Pool {
        final GunData data;
        final AmmoConsumer consumer;
        int wanted, loaded, step;
        /** Guns whose loaded rounds are already in {@link #loaded}. */
        final java.util.Set<String> countedGuns = new java.util.HashSet<>();

        Pool(GunData data, AmmoConsumer consumer) {
            this.data = data;
            this.consumer = consumer;
        }
    }

    /**
     * The F8 "spawn fully loaded" option: a new vehicle gets its whole combat load at once (plus a full
     * tank). Called on the vehicle's first server tick.
     */
    public static void onFirstTick(VehicleEntity vehicle) {
        if (vehicle.level().isClientSide() || vehicle.getPersistentData().getBoolean(BORN)) return;
        // Persistent data is saved, so a vehicle loaded back from disk is never treated as new again.
        vehicle.getPersistentData().putBoolean(BORN, true);
        if (FcpWorldSettings.current().fillNewVehicles()) fillCombatLoad(vehicle);
    }

    public static void fillCombatLoad(VehicleEntity vehicle) {
        rearm(vehicle, true);
        if (vehicle.hasEnergyStorage()) vehicle.setEnergy(vehicle.getMaxEnergy());
    }

    /** One magazine per weapon (and one decoy reload), or everything missing when {@code full}. */
    private static void rearm(VehicleEntity vehicle, boolean full) {
        IItemHandler inv = vehicle.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
        if (inv == null) return;
        rearmDecoys(vehicle, inv, full);
        VehicleArmor.Profile p = VehicleArmor.profile(vehicle);
        if (p == null || p.ammo().isEmpty()) return;
        Map<String, GunData> guns = vehicle.getGunDataMap();
        Map<String, Pool> pools = new LinkedHashMap<>();

        for (Map.Entry<String, VehicleArmor.Loadout> e : p.ammo().entrySet()) {
            String key = e.getKey();
            int hash = key.indexOf('#');
            String gun = hash < 0 ? key : key.substring(0, hash);
            int index = hash < 0 ? 0 : Integer.parseInt(key.substring(hash + 1));
            GunData data = guns.get(gun);
            if (data == null) continue;
            List<AmmoConsumer> consumers = data.get(GunProp.AMMO_CONSUMER);
            if (consumers == null || index >= consumers.size()) continue;
            AmmoConsumer consumer = consumers.get(index);
            if (consumer.getAmmo() == null) continue;

            Pool pool = pools.computeIfAbsent(consumer.getAmmo(), k -> new Pool(data, consumer));
            pool.wanted += e.getValue().total();
            // Every ammo type of this gun firing the same item (e.g. a missile's second guidance mode) draws on
            // the same stock: count what's loaded under all of them, once per gun.
            if (pool.countedGuns.add(gun)) {
                for (int j = 0; j < consumers.size(); j++) {
                    if (consumer.getAmmo().equals(consumers.get(j).getAmmo())) pool.loaded += loaded(data, consumers, j);
                }
            }
            pool.step += e.getValue().rounds();
        }

        for (Pool pool : pools.values()) {
            int load = Math.max(1, pool.consumer.getLoadAmount());
            int carried = pool.consumer.count(pool.data, inv) * load;
            int missing = pool.wanted - pool.loaded - carried;
            int add = full ? missing : Math.min(pool.step, missing);
            if (add <= 0) continue;
            insert(inv, pool.consumer, add, load);
        }
        for (GunData data : guns.values()) data.cachedBackupAmmo = -1;
    }

    /** Smoke launchers carry {@link #SMOKE_SALVOS} salvos (one item each); helicopters one magazine of {@link #FLARES} flares. */
    private static final int SMOKE_SALVOS = 2, FLARES = 8;

    /** Tops up smoke (one salvo per step) or flares; what's already loaded in the launcher counts. */
    private static void rearmDecoys(VehicleEntity vehicle, IItemHandler inv, boolean full) {
        if (!vehicle.hasDecoy()) return;
        boolean smoke = vehicle.hasSmokeDecoy();
        Item item = smoke ? ModItems.VEHICLE_SMOKE_AMMO.get() : ModItems.FLYING_FLARE_AMMO.get();
        int carried = 0;
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.is(item)) carried += stack.getCount();
        }
        int missing = (smoke ? SMOKE_SALVOS : FLARES) - vehicle.getDecoyCount() - carried;
        int add = full || !smoke ? missing : Math.min(1, missing);
        if (add > 0) ItemHandlerHelper.insertItemStacked(inv, new ItemStack(item, add), false);
    }

    /** Rounds sitting in the gun for ammo type {@code index}: the magazine if it's selected, else its stored slot. */
    private static int loaded(GunData data, List<AmmoConsumer> consumers, int index) {
        int selected = Math.min(data.selectedAmmoType.get(), consumers.size() - 1);
        if (selected == index) return data.ammo.get();
        String slot = consumers.get(index).getAmmoSlot();
        // Two types on the same slot share one magazine: only the selected one is in it.
        if (selected >= 0 && slot.equals(consumers.get(selected).getAmmoSlot())) return 0;
        return data.ammoSlot.getAmmo(slot);
    }

    /** Small-arms rounds go into an ammo box (thousands of rounds in one slot); everything else as items. */
    private static void insert(IItemHandler inv, AmmoConsumer consumer, int rounds, int load) {
        Ammo type = consumer.getPlayerAmmoType();
        if (type != null) {
            rounds -= fillAmmoBoxes(inv, type, rounds);
            if (rounds > 0) {
                ItemStack box = new ItemStack(ModItems.AMMO_BOX.get());
                int put = Math.min(rounds, type.getAmmoBoxLimit());
                type.set(box, put);
                if (ItemHandlerHelper.insertItemStacked(inv, box, false).isEmpty()) rounds -= put;
            }
            if (rounds > 0) consumer.withdraw(inv, rounds);
            return;
        }
        consumer.withdraw(inv, (rounds + load - 1) / load);
    }

    private static int fillAmmoBoxes(IItemHandler inv, Ammo type, int rounds) {
        int added = 0;
        for (int i = 0; i < inv.getSlots() && added < rounds; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!(stack.getItem() instanceof AmmoBoxItem) || stack.getCount() != 1) continue;
            int room = type.getAmmoBoxLimit() - type.get(stack);
            int put = Math.min(room, rounds - added);
            if (put > 0 && type.add(stack, put)) added += put;
        }
        return added;
    }
}
