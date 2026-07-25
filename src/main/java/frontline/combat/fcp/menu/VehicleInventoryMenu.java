package frontline.combat.fcp.menu;

import frontline.combat.fcp.entity.vehicle.VehicleInventory;
import frontline.combat.fcp.entity.vehicle.VehicleInventory.InventoryStyle;
import frontline.combat.fcp.init.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * One menu for every vehicle hold — any size, either style.
 *
 * Vanilla's ChestMenu can't do this job: it caps at 6 rows, and its plain Slots accept
 * anything (Slot.mayPlace is hardcoded true), so a container-level filter would bind
 * hoppers but never the GUI.
 *
 * ── NONE ─────────────────────────────────────────────────────────────────────
 * No hold at all: zero cargo slots, so the panel is just the player's inventory. That falls
 * straight out of the layout maths (0 rows -> a 114px window) and needs no special case,
 * beyond shift-click shuffling within the player's own inventory since there's nowhere else
 * to send things.
 *
 * ── GRID ─────────────────────────────────────────────────────────────────────
 * Rows come from the container. Past {@link #MAX_VISIBLE_ROWS} the window would run off the
 * bottom of the screen, so it shows a fixed viewport and scrolls.
 *
 * The slots do NOT move — Slot.x/y are final in vanilla and Forge doesn't change that, so a
 * scrolled slot can't be repositioned. Instead the slots stay put and a {@link ScrollView}
 * slides the CONTAINER underneath them: slot i reads {@code backing[i + scrollRow*9]}.
 *
 * ── BULK ─────────────────────────────────────────────────────────────────────
 * A silo for one item type, drawn as a single icon plus a count. Underneath it is still an
 * ordinary container: every slot is a REAL menu slot, just parked off-screen. That is what
 * keeps hoppers, shift-click and drop-on-destroy working identically to a grid — the only
 * difference is that nothing is drawn and the player deposits/withdraws through the icon.
 *
 * ── Talking to the server ────────────────────────────────────────────────────
 * Scrolling and bulk deposit/withdraw both change server-side state, so both reuse vanilla's
 * menu-button packet rather than giving FCP a network channel: the screen calls
 * handleInventoryButtonClick(containerId, id) -> clickMenuButton here -> returning true
 * makes vanilla broadcastChanges(), which resyncs the slots AND the carried stack.
 */
public class VehicleInventoryMenu extends AbstractContainerMenu {

    public static final int COLS = 9;
    /**
     * Tallest GRID viewport, in rows. 7 rows = 63 slots = a 240px window, the most that
     * reliably fits on screen; anything taller scrolls.
     */
    public static final int MAX_VISIBLE_ROWS = 7;
    /** Each ROW of bulk channels, in 18px layout rows (36px per row). */
    public static final int BULK_ROWS_PER_CHANNEL = 2;
    /** Most bulk channels shown side by side before wrapping to another row. */
    public static final int MAX_CHANNEL_COLS = 3;

    /** Button ids. Scroll uses 0..maxScrollRow, so bulk actions sit well clear of it. */
    public static final int BUTTON_BULK_DEPOSIT = 1000;
    /** Withdraw from channel c into the HAND = BUTTON_BULK_WITHDRAW_BASE + c. */
    public static final int BUTTON_BULK_WITHDRAW_BASE = 1100;
    /** Shift-click: move a stack from channel c straight into the PLAYER'S inventory. */
    public static final int BUTTON_BULK_QUICK_MOVE_BASE = 1200;
    /** Sort/compact the GRID hold. Clear of the scroll ids (0..maxScrollRow) and the bulk ids. */
    public static final int BUTTON_SORT = 2000;

    /** Real slots the player can never see or click. */
    private static final int OFF_SCREEN = -2000;
    /** How far you can stray before the hold closes on you. */
    private static final double REACH = 8.0;

    private final Container backing;
    @Nullable
    private final ScrollView view; // GRID only
    @Nullable
    private final VehicleInventory vehicle;
    private final InventoryStyle style;
    private final int channels;
    private final int slotsPerChannel;
    private final int totalRows;
    private final int visibleRows;
    private final int cargoSlots;

    private int scrollRow;

    /**
     * Client side. The vehicle is resolved from its network id so the same filter applies
     * locally — without it the client would show an item dropping into a slot the server
     * then rejects, which looks like the GUI is broken.
     */
    public static VehicleInventoryMenu fromNetwork(int id, Inventory playerInv, FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        int totalSize = buf.readVarInt();
        int styleOrdinal = buf.readVarInt();
        int channels = buf.readVarInt();
        Entity entity = playerInv.player.level().getEntity(entityId);
        VehicleInventory found = entity instanceof VehicleInventory vi ? vi : null;
        InventoryStyle style = InventoryStyle.values()[
                Mth.clamp(styleOrdinal, 0, InventoryStyle.values().length - 1)];
        return new VehicleInventoryMenu(id, playerInv, new SimpleContainer(totalSize), found,
                style, channels);
    }

    /** Server side: {@code backing} is the vehicle's real container. */
    public VehicleInventoryMenu(int id, Inventory playerInv, Container backing,
                                @Nullable VehicleInventory vehicle, InventoryStyle style, int channels) {
        super(ModMenus.VEHICLE_INVENTORY.get(), id);
        this.backing = backing;
        this.vehicle = vehicle;
        this.style = style;
        this.channels = Math.max(1, channels);
        this.slotsPerChannel = Math.max(1, backing.getContainerSize() / this.channels);
        this.totalRows = backing.getContainerSize() / COLS;
        backing.startOpen(playerInv.player);

        int layoutRows;
        if (style == InventoryStyle.BULK) {
            // Every slot is real so the container behaves normally; none are drawn.
            this.visibleRows = 0;
            this.view = null;
            this.cargoSlots = backing.getContainerSize();
            layoutRows = bulkRows() * BULK_ROWS_PER_CHANNEL;
            for (int i = 0; i < this.cargoSlots; i++) {
                this.addSlot(new CargoSlot(backing, i, OFF_SCREEN, OFF_SCREEN, i / this.slotsPerChannel));
            }
        } else {
            this.visibleRows = Math.min(this.totalRows, MAX_VISIBLE_ROWS);
            this.view = new ScrollView(backing, this.visibleRows * COLS);
            this.cargoSlots = this.visibleRows * COLS;
            layoutRows = this.visibleRows;
            for (int row = 0; row < this.visibleRows; row++) {
                for (int col = 0; col < COLS; col++) {
                    this.addSlot(new CargoSlot(this.view, col + row * COLS,
                            8 + col * 18, 18 + row * 18, -1));
                }
            }
        }

        // Player inventory + hotbar. Same maths ChestMenu uses, generalised.
        int offset = (layoutRows - 4) * 18;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offset));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 161 + offset));
        }
    }

    public InventoryStyle getStyle() {
        return this.style;
    }

    public int getChannels() {
        return this.channels;
    }

    /**
     * Columns in the bulk channel grid.
     *
     * Kept as square as possible with a floor of 2 and a ceiling of {@link #MAX_CHANNEL_COLS}:
     * 1-2 channels sit in one row of two, 3-4 fill a 2x2, 5-6 a 2x3, and beyond that it grows
     * downwards — 7-9 make a 3x3, 10-12 a 4x3.
     *
     * This lives on the MENU rather than the screen because it decides the panel's height,
     * and the menu positions the player-inventory slots against that same height — if the
     * two disagreed the slots would sit off the drawn panel.
     */
    public int bulkCols() {
        int square = (int) Math.ceil(Math.sqrt(this.channels));
        return Math.max(2, Math.min(MAX_CHANNEL_COLS, square));
    }

    /** Rows in the bulk channel grid. */
    public int bulkRows() {
        int cols = bulkCols();
        return (this.channels + cols - 1) / cols;
    }

    /** Layout rows (18px each). BULK draws no slots and sizes its band from its channel grid. */
    public int getVisibleRows() {
        return this.style == InventoryStyle.BULK
                ? bulkRows() * BULK_ROWS_PER_CHANNEL
                : this.visibleRows;
    }

    public int getScrollRow() {
        return this.scrollRow;
    }

    public int maxScrollRow() {
        return this.style == InventoryStyle.BULK ? 0 : Math.max(0, this.totalRows - this.visibleRows);
    }

    public boolean isScrollable() {
        return maxScrollRow() > 0;
    }

    /** Client-side knob state. The contents follow when the server answers. */
    public void setScrollRow(int row) {
        this.scrollRow = Mth.clamp(row, 0, maxScrollRow());
    }

    // ── Bulk view (computed from the synced slots, so it's correct on both sides) ──

    private int channelStart(int channel) {
        return channel * this.slotsPerChannel;
    }

    private int channelEnd(int channel) {
        return Math.min(channelStart(channel) + this.slotsPerChannel, this.cargoSlots);
    }

    /** The item type in a channel, or empty. Read from the synced slots, so both sides agree. */
    public ItemStack bulkTypeOf(int channel) {
        for (int i = channelStart(channel); i < channelEnd(channel); i++) {
            ItemStack stack = this.slots.get(i).getItem();
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    /** Items stored in a channel. */
    public int bulkCountOf(int channel) {
        int total = 0;
        for (int i = channelStart(channel); i < channelEnd(channel); i++) {
            total += this.slots.get(i).getItem().getCount();
        }
        return total;
    }

    /** Items that fit in a channel, given what's in it (stack limits differ per item). */
    public int bulkCapacityOf(int channel) {
        ItemStack type = bulkTypeOf(channel);
        int perSlot = type.isEmpty() ? 64 : type.getMaxStackSize();
        return this.slotsPerChannel * perSlot;
    }

    /** Which channel already holds this item, or -1. */
    private int channelOf(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        for (int channel = 0; channel < this.channels; channel++) {
            ItemStack type = bulkTypeOf(channel);
            if (!type.isEmpty() && ItemStack.isSameItemSameTags(type, stack)) return channel;
        }
        return -1;
    }

    /**
     * One place deciding what any cargo slot accepts — GUI, shift-click and client
     * prediction. Mirrors CamoVehicleBase.acceptsFromOutside so hoppers and players can
     * never diverge.
     */
    private boolean canAccept(ItemStack stack, int channel) {
        if (this.vehicle != null) {
            if (!this.vehicle.canPlaceIntoStorage()) return false; // extract-only hold
            if (!this.vehicle.canStoreItem(stack)) return false;
        }
        if (this.style != InventoryStyle.BULK) return true;

        ItemStack type = bulkTypeOf(channel);
        if (!type.isEmpty()) return ItemStack.isSameItemSameTags(type, stack);
        // An unclaimed channel takes anything that isn't already in another one, so one
        // item can never occupy two channels.
        return channelOf(stack) < 0;
    }

    /** A cargo slot that defers to the vehicle's rules (and its channel's type). */
    private class CargoSlot extends Slot {
        private final int channel; // -1 for GRID

        CargoSlot(Container container, int index, int x, int y, int channel) {
            super(container, index, x, y);
            this.channel = channel;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return VehicleInventoryMenu.this.canAccept(stack, this.channel);
        }
    }

    // ── Server-side actions, arriving on vanilla's menu-button packet ─────────────

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.style == InventoryStyle.BULK) {
            if (id == BUTTON_BULK_DEPOSIT) return depositCarried();

            int quick = id - BUTTON_BULK_QUICK_MOVE_BASE;
            if (quick >= 0 && quick < this.channels) return quickMoveOut(quick);

            int channel = id - BUTTON_BULK_WITHDRAW_BASE;
            if (channel >= 0 && channel < this.channels) return withdrawStack(channel);
            return false;
        }

        if (id == BUTTON_SORT) return sortGrid();

        if (!isScrollable() || this.view == null) return false;
        int row = Mth.clamp(id, 0, maxScrollRow());
        this.scrollRow = row;
        this.view.setRowOffset(row);
        return true;
    }

    /**
     * Sort and compact the GRID hold: merge same-item stacks, then order them by item id so
     * they pack from the top-left. Operates on the WHOLE backing container (not just the
     * visible window), so it works regardless of scroll position. Returning true makes
     * vanilla broadcastChanges() and push the new arrangement to the client.
     */
    private boolean sortGrid() {
        int size = this.backing.getContainerSize();

        // Pull everything out and merge equal stacks up to their max size.
        java.util.List<ItemStack> merged = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            ItemStack stack = this.backing.getItem(i);
            if (stack.isEmpty()) continue;
            ItemStack copy = stack.copy();
            for (ItemStack m : merged) {
                if (m.getCount() < m.getMaxStackSize() && ItemStack.isSameItemSameTags(m, copy)) {
                    int move = Math.min(m.getMaxStackSize() - m.getCount(), copy.getCount());
                    m.grow(move);
                    copy.shrink(move);
                    if (copy.isEmpty()) break;
                }
            }
            if (!copy.isEmpty()) merged.add(copy);
        }

        // Stable order: by registry id, then by count descending so fuller stacks lead.
        merged.sort((a, b) -> {
            int byId = itemKey(a).compareTo(itemKey(b));
            return byId != 0 ? byId : Integer.compare(b.getCount(), a.getCount());
        });

        // Write back compacted from slot 0; clear the rest.
        for (int i = 0; i < size; i++) {
            this.backing.setItem(i, i < merged.size() ? merged.get(i) : ItemStack.EMPTY);
        }
        this.backing.setChanged();
        return true;
    }

    private static String itemKey(ItemStack stack) {
        net.minecraft.resources.ResourceLocation id =
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }

    /** Put the held stack into the silo; whatever won't fit stays in hand. */
    private boolean depositCarried() {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return false;
        // moveItemStackTo shrinks the stack as it goes and honours mayPlace, so the
        // single-type rule applies here too.
        moveItemStackTo(carried, 0, this.cargoSlots, false);
        setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
        return true;
    }

    /**
     * Pull up to one stack out of a channel. Taking OUT is never gated by
     * canPlaceIntoStorage — that toggle only keeps things from coming in.
     */
    private ItemStack takeStack(int channel) {
        ItemStack result = ItemStack.EMPTY;
        for (int i = channelStart(channel); i < channelEnd(channel); i++) {
            if (!result.isEmpty() && result.getCount() >= result.getMaxStackSize()) break;

            Slot slot = this.slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            if (result.isEmpty()) {
                result = stack.copy();
                int take = Math.min(result.getMaxStackSize(), stack.getCount());
                result.setCount(take);
                stack.shrink(take);
            } else if (ItemStack.isSameItemSameTags(result, stack)) {
                int take = Math.min(result.getMaxStackSize() - result.getCount(), stack.getCount());
                result.grow(take);
                stack.shrink(take);
            } else {
                continue;
            }

            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    /**
     * Put a stack back where it came from, bypassing mayPlace — this is undoing our own
     * extraction, so an extract-only hold must not refuse it.
     */
    private void returnToChannel(int channel, ItemStack stack) {
        for (int i = channelStart(channel); i < channelEnd(channel) && !stack.isEmpty(); i++) {
            Slot slot = this.slots.get(i);
            ItemStack current = slot.getItem();
            if (current.isEmpty()) {
                slot.set(stack.split(Math.min(stack.getCount(), stack.getMaxStackSize())));
            } else if (ItemStack.isSameItemSameTags(current, stack)) {
                int space = current.getMaxStackSize() - current.getCount();
                if (space <= 0) continue;
                int moved = Math.min(space, stack.getCount());
                current.grow(moved);
                stack.shrink(moved);
                slot.setChanged();
            }
        }
    }

    /** Take up to one stack out of a channel, into the hand. */
    private boolean withdrawStack(int channel) {
        if (!getCarried().isEmpty()) return false;
        ItemStack result = takeStack(channel);
        if (result.isEmpty()) return false;
        setCarried(result);
        return true;
    }

    /**
     * Shift-click on a silo icon: send a stack straight to the player's inventory rather
     * than into the hand — the same thing shift-clicking a normal slot would do. Anything
     * that doesn't fit goes back in the channel.
     */
    private boolean quickMoveOut(int channel) {
        ItemStack result = takeStack(channel);
        if (result.isEmpty()) return false;

        moveItemStackTo(result, this.cargoSlots, this.slots.size(), true);
        if (!result.isEmpty()) {
            returnToChannel(channel, result); // player inventory was full
        }
        return true;
    }

    /**
     * A sliding window onto a bigger container: index i maps to {@code i + rowOffset*9}.
     * Only the server ever offsets it; on the client it stays at 0 and simply reads the
     * window the server sent. GRID only.
     */
    private static class ScrollView implements Container {
        private final Container backing;
        private final int size;
        private int rowOffset;

        ScrollView(Container backing, int size) {
            this.backing = backing;
            this.size = size;
        }

        void setRowOffset(int rowOffset) {
            this.rowOffset = rowOffset;
        }

        private int map(int index) {
            return index + this.rowOffset * COLS;
        }

        @Override
        public int getContainerSize() {
            return this.size;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < this.size; i++) {
                if (!getItem(i).isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return this.backing.getItem(map(index));
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            return this.backing.removeItem(map(index), count);
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            return this.backing.removeItemNoUpdate(map(index));
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            this.backing.setItem(map(index), stack);
        }

        @Override
        public int getMaxStackSize() {
            return this.backing.getMaxStackSize();
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return this.backing.canPlaceItem(map(index), stack);
        }

        @Override
        public void setChanged() {
            this.backing.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return this.backing.stillValid(player);
        }

        @Override
        public void clearContent() {
            this.backing.clearContent();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (this.cargoSlots == 0) {
                // NONE: there's no hold to shift things into, so fall back to what vanilla's
                // own inventory screen does and shuffle between backpack and hotbar —
                // otherwise shift-click would silently do nothing.
                int hotbarStart = this.slots.size() - 9;
                if (index < hotbarStart) {
                    if (!this.moveItemStackTo(stack, hotbarStart, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, 0, hotbarStart, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < this.cargoSlots) {
                if (!this.moveItemStackTo(stack, this.cargoSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, this.cargoSlots, false)) {
                // moveItemStackTo honours mayPlace on empty slots, so neither the vehicle
                // filter nor the silo's single-type rule can be bypassed by shift-clicking.
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        // Close if the vehicle is destroyed or drives off without you.
        if (this.vehicle instanceof Entity entity) {
            return entity.isAlive() && entity.distanceTo(player) < REACH;
        }
        return this.backing.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.backing.stopOpen(player);
    }
}