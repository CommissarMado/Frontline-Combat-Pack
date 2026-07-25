package frontline.combat.fcp.entity.vehicle;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * A vehicle that carries cargo.
 *
 * Implemented by CamoVehicleBase, so EVERY FCP vehicle has it — but it stays dormant until
 * a vehicle opts in. The container, GUI, filtering, saving and drop-on-destroy are handled
 * by the base; there is no per-vehicle menu, screen, texture or registration.
 *
 * <pre>
 * // a seed hopper: one big filtered grid
 * &#64;Override public int inventorySize() { return 81; }
 * &#64;Override public boolean canStoreItem(ItemStack s) { return isSeed(s); }
 *
 * // a combine's tank: crop AND seed, filled by the machine, emptied by the player
 * &#64;Override public InventoryStyle inventoryStyle()  { return InventoryStyle.BULK; }
 * &#64;Override public int inventorySize()              { return 54; }
 * &#64;Override public int bulkChannels()               { return 2; }
 * &#64;Override public boolean canPlaceIntoStorage()    { return false; }
 * </pre>
 */
public interface VehicleInventory {

    /** Which UI a hold uses. GRID and BULK are ordinary containers underneath — hoppers,
     *  shift-click and drop-on-destroy behave the same either way; only the presentation
     *  differs. */
    enum InventoryStyle {
        /**
         * No vehicle storage at all — the screen opens showing ONLY the player's own
         * inventory, with no compartment above it. For a vehicle like the tractor that
         * should have a screen but nothing to put in it. inventorySize() is ignored, no
         * container is allocated, nothing is saved and nothing drops.
         */
        NONE,
        /** Normal slot grid. */
        GRID,
        /**
         * Silo: holds a small number of item TYPES in bulk, each shown as one icon with a
         * running count instead of a grid of stacks. The slots still exist underneath (so
         * hoppers and shift-click work exactly as before) — they're just not drawn.
         */
        BULK
    }

    // ── Size & style ────────────────────────────────────────────────────────────

    /**
     * Number of cargo slots. 0 (the default) means this vehicle has no inventory — and no
     * screen either, unless the style is {@link InventoryStyle#NONE}, which ignores this.
     *
     * GRID: rounded UP to a multiple of 9 (the grid is 9 wide) — ask for 20 and you get 27.
     * Up to 63 (7 rows) is shown at once; beyond that it becomes a 7-row viewport with a
     * scrollbar, so 81, 108 or more all just work.
     *
     * BULK: this is the depth of EACH channel, not the total — the container is sized to
     * size x {@link #bulkChannels()}. Capacity per channel is roughly size x 64 items, so
     * a two-channel hold at 125 carries 8,000 of the crop AND 8,000 of the seed. Adding a
     * channel adds capacity instead of splitting what's there. The slot count is never
     * shown, so any value is fine.
     */
    int inventorySize();

    /** Grid of stacks, or a bulk silo. Default: grid. */
    default InventoryStyle inventoryStyle() {
        return InventoryStyle.GRID;
    }

    /**
     * How many DISTINCT item types a BULK hold can carry at once — one independent
     * compartment each, drawn as its own icon and counter. Default 1.
     *
     * A combine returns 2: one channel fills with the crop, the other with its seed. The
     * channels are self-assigning — the first item to land in an empty one claims it, and
     * an item that's already in a channel can never start a second one, so the two never
     * blur together. Ignored by GRID.
     */
    default int bulkChannels() {
        return 1;
    }

    // ── Access rules ────────────────────────────────────────────────────────────

    /**
     * Whether anything OUTSIDE the vehicle may put items in — players (by hand or
     * shift-click) and hoppers alike. Default true.
     *
     * Set false for a hold the machine fills itself and the player only empties, such as a
     * combine's tank. The vehicle's own code is unaffected: {@link #storeItem(ItemStack)}
     * deliberately bypasses this, so the harvester keeps loading its tank while nobody can
     * stuff junk into it. Taking items OUT is never restricted.
     */
    default boolean canPlaceIntoStorage() {
        return true;
    }

    /**
     * Per-vehicle content filter — the ONLY thing a vehicle has to write to restrict its
     * cargo. Enforced everywhere at once: the GUI slots, shift-clicking and hoppers.
     * Default: carries anything.
     *
     * Deliberately code rather than data: asking the item itself (does Forge think farmland
     * would grow this?) covers modded content automatically, where a list of item ids would
     * only ever know what someone remembered to add.
     *
     * BULK holds additionally reject anything that isn't already in one of their channels
     * (once every channel is claimed); that's automatic and needn't be written here.
     */
    default boolean canStoreItem(ItemStack stack) {
        return true;
    }

    /** The live container. Created on first use, sized from the hooks above. */
    SimpleContainer getVehicleInventory();

    /** Whether this vehicle actually stores anything (drives NBT, drops, hopper access). */
    default boolean hasVehicleInventory() {
        return inventoryStyle() != InventoryStyle.NONE && inventorySize() > 0;
    }

    /**
     * Whether openVehicleInventory() has a screen to show. NONE has no storage but still
     * opens — that's the whole point of it.
     */
    default boolean opensVehicleScreen() {
        return inventoryStyle() == InventoryStyle.NONE || hasVehicleInventory();
    }

    // ── Bulk channels ───────────────────────────────────────────────────────────

    /** Slots backing each channel — i.e. inventorySize(). The container is channels x this. */
    default int bulkSlotsPerChannel() {
        return getVehicleInventory().getContainerSize() / Math.max(1, bulkChannels());
    }

    /** The item type held in a channel, or empty if the channel is unclaimed. */
    default ItemStack bulkTypeOf(int channel) {
        SimpleContainer inventory = getVehicleInventory();
        int per = bulkSlotsPerChannel();
        for (int i = channel * per; i < (channel + 1) * per && i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    /** Which channel already holds this item, or -1 if none does. */
    default int bulkChannelOf(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        for (int channel = 0; channel < Math.max(1, bulkChannels()); channel++) {
            ItemStack type = bulkTypeOf(channel);
            if (!type.isEmpty() && ItemStack.isSameItemSameTags(type, stack)) return channel;
        }
        return -1;
    }

    /** The first channel nothing has claimed yet, or -1 if they're all taken. */
    default int firstFreeBulkChannel() {
        for (int channel = 0; channel < Math.max(1, bulkChannels()); channel++) {
            if (bulkTypeOf(channel).isEmpty()) return channel;
        }
        return -1;
    }

    /** Convenience for single-channel silos. */
    default ItemStack bulkStoredType() {
        return bulkTypeOf(0);
    }

    // ── Internal loading ────────────────────────────────────────────────────────

    /**
     * Put items in from the VEHICLE'S OWN code — a harvester loading its tank, say.
     * Returns whatever didn't fit.
     *
     * Deliberately ignores {@link #canPlaceIntoStorage()}, which exists to keep players and
     * hoppers out, not the machine itself. It does respect bulk channels, routing the stack
     * to the channel already holding that item or claiming a free one.
     */
    default ItemStack storeItem(ItemStack stack) {
        if (stack.isEmpty() || !hasVehicleInventory()) return stack;
        SimpleContainer inventory = getVehicleInventory();

        if (inventoryStyle() != InventoryStyle.BULK) {
            return inventory.addItem(stack);
        }

        int channel = bulkChannelOf(stack);
        if (channel < 0) channel = firstFreeBulkChannel();
        if (channel < 0) return stack; // every channel is holding something else

        int per = bulkSlotsPerChannel();
        int end = Math.min((channel + 1) * per, inventory.getContainerSize());

        for (int i = channel * per; i < end && !stack.isEmpty(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot.isEmpty()) {
                inventory.setItem(i, stack.split(Math.min(stack.getCount(), stack.getMaxStackSize())));
            } else if (ItemStack.isSameItemSameTags(slot, stack)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space <= 0) continue;
                int moved = Math.min(space, stack.getCount());
                slot.grow(moved);
                stack.shrink(moved);
            }
        }
        inventory.setChanged();
        return stack;
    }
}