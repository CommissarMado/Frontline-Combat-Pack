package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.init.ModItems;
import frontline.combat.fcp.init.ModSounds;
import frontline.combat.fcp.entity.vehicle.VehicleInventory.InventoryStyle;
import frontline.combat.fcp.menu.VehicleInventoryMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Artillery flavour of {@link CamoVehicleBase}.
 *
 * Identical camo + hold behaviour, but rooted at SBW's {@link ArtilleryEntity} instead of
 * GeoVehicleEntity, because the Artillery Indicator only binds to that type
 * (ArtilleryIndicatorItem.bind checks `entity is ArtilleryEntity`). Java has single
 * inheritance and the two SBW bases are siblings, so an artillery-capable FCP vehicle cannot
 * simply extend CamoVehicleBase.
 *
 * Inheriting ArtilleryEntity also brings: firing-parameters targeting, trajectory solving
 * (calculateLaunchVector), barrel recoil animation state, turret lock while moving, and the
 * auto-reload that tops up the gun from the turret controller's own inventory.
 *
 * canBind() is true here, which is what actually exposes these vehicles to the indicator.
 */
public abstract class CamoArtilleryBase extends ArtilleryEntity implements ICamoVehicle, VehicleInventory {

    /** Allows the Artillery Indicator to bind to this vehicle for remote aiming. */
    @Override
    public boolean canBind() {
        return true;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        autoloadFromHold();
    }

    /**
     * Feeds the gun from the vehicle's own cargo hold.
     *
     * SBW's autoload (ArtilleryEntity.baseTick) only runs when a PLAYER is sitting in the turret
     * controller seat, and it draws from that player's personal inventory. Artillery fired
     * remotely through the Artillery Indicator has an empty seat, so it never reloaded and went
     * silent after its first round. This tops the loading tray up from getVehicleInventory()
     * instead, so a piece stocked with shells keeps firing while unmanned.
     *
     * Deliberately skipped when a player IS seated: SBW's own logic already handles that case,
     * and running both would consume two rounds per tick.
     */
    private void autoloadFromHold() {
        if (this.level().isClientSide() || this.isWreck() || !hasVehicleInventory()) return;
        if (getNthEntity(getTurretControllerIndex()) instanceof Player) return;

        GunData gunData = getGunData("Main");
        if (gunData == null) return;

        ItemStack ammoStack = gunData.selectedAmmoConsumer().stack();
        if (ammoStack.isEmpty()) return;
        Item ammoItem = ammoStack.getItem();

        // Slot 0 of SBW's native container is the loading tray the gun reloads from.
        if (getItems().isEmpty()) return;
        ItemStack tray = getItems().get(0);
        int count = tray.getCount();
        if (count >= Math.min(this.getMaxStackSize(), ammoStack.getMaxStackSize())) return;
        // Never mix ammo types in the tray.
        if (!tray.isEmpty() && !tray.is(ammoItem)) return;

        SimpleContainer hold = getVehicleInventory();
        for (int i = 0; i < hold.getContainerSize(); i++) {
            ItemStack held = hold.getItem(i);
            if (held.isEmpty() || !held.is(ammoItem)) continue;

            held.shrink(1);
            if (held.isEmpty()) hold.setItem(i, ItemStack.EMPTY);
            hold.setChanged();
            setItem(0, ammoStack.copyWithCount(count + 1));
            return; // one round per tick, same cadence as SBW's own autoload
        }
    }


    private static final EntityDataAccessor<Integer> CAMO_TYPE = SynchedEntityData.defineId(CamoArtilleryBase.class, EntityDataSerializers.INT);

    public CamoArtilleryBase(EntityType<? extends VehicleEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CAMO_TYPE, 0);
    }

    /**
     * The texture array is split into two halves:
     * [0 .. camoCount-1] = normal camo textures
     * [camoCount .. total-1] = wrecked variants, one per camo in the same order
     *
     * camoCount = ceil(totalTextures / 2)
     *
     * When wrecked, the modifier (camoCount) is added to the current camo index
     * to land on the corresponding wrecked texture.
     */
    public ResourceLocation getCurrentTexture() {
        ResourceLocation[] textures = getCamoTextures();
        int total = textures.length;
        // Round up so an odd total always favours the camo side
        int camoCount = (int) Math.ceil(total / 2.0);
        int index = getCamoType();
        if (index < 0 || index >= camoCount) index = 0;

        if (this.isWreck()) {
            int wreckedIndex = index + camoCount;
            // Safety clamp in case of an odd total leaving one fewer wrecked texture
            if (wreckedIndex >= total) wreckedIndex = total - 1;
            return textures[wreckedIndex];
        }

        return textures[index];
    }

    @Override
    public int getCamoType() {
        return this.entityData.get(CAMO_TYPE);
    }

    @Override
    public void setCamoType(int camoType) {
        this.entityData.set(CAMO_TYPE, camoType);
    }

    @Override
    public void cycleCamo() {
        int total = getCamoTextures().length;
        int camoCount = (int) Math.ceil(total / 2.0);
        int current = getCamoType();
        // Only cycle within the normal camo range, never into the wrecked half
        setCamoType((current + 1) % camoCount);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).is(ModItems.SPRAY.get())) {
            if (!this.level().isClientSide) {
                cycleCamo();
                String[] camoNames = getCamoNames();
                int camoType = getCamoType();
                String camoName = (camoType >= 0 && camoType < camoNames.length)
                        ? camoNames[camoType]
                        : "Unknown";

                player.displayClientMessage(
                        Component.translatable("message.fcp.camo_changed", camoName).withStyle(ChatFormatting.GREEN),
                        true
                );
                this.level().playSound(null, this.blockPosition(),
                        ModSounds.SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        // Vehicle hold: driveables open on SHIFT-click (standing) or E (driving); trailers
        // override opensHoldOnPlainClick() to open on a plain click instead. Handled here in
        // the core so no individual vehicle needs its own interact() for this.
        InteractionResult hold = openHoldInteraction(player, hand);
        if (hold != null) return hold;

        return super.interact(player, hand);
    }

    // ── Vehicle cargo ───────────────────────────────────────────────────────────
    // Every FCP vehicle inherits this, but it stays dormant until a vehicle overrides
    // inventorySize(). One menu + one screen + one texture serve all of them, at any size,
    // with per-vehicle filtering via canStoreItem(). Adding cargo to a vehicle is:
    //
    //     @Override public int inventorySize() { return 81; }
    //     @Override public boolean canStoreItem(ItemStack s) { return ...; }
    //
    // Opening is handled by the core interact() above — no per-vehicle interact() needed.

    private SimpleContainer vehicleInventory;

    /**
     * Exposes {@link #getVehicleInventory()} as the entity's ITEM_HANDLER capability, so
     * SBW's ammo system (count, reload, consume — all via getCapability(ITEM_HANDLER)) reads
     * and consumes ammo straight from the SAME container the GUI edits. One source of truth,
     * no mirroring. InvWrapper honours the container's canPlaceItem (our filter) on insert;
     * extraction (firing) is unrestricted. Created lazily, invalidated in invalidateCaps().
     */
    private LazyOptional<IItemHandler> ammoItemHandler = LazyOptional.empty();

    /** No cargo by default. Override per vehicle. */
    @Override
    public int inventorySize() {
        return 0;
    }

    /**
     * Container size.
     *
     * GRID rounds up to whole 9-wide rows so it matches the drawn grid.
     *
     * BULK treats inventorySize() as the depth of EACH channel and multiplies: a hold that
     * carries two crops at 8,000 apiece is 8,000-per-channel x 2, not 8,000 shared out.
     * Adding a channel therefore adds capacity rather than halving what's already there.
     * No multiple-of-9 rounding — there's no grid to line up with.
     */
    private int vehicleInventorySlots() {
        if (inventoryStyle() == InventoryStyle.NONE) return 0; // screen only, no storage
        int perChannel = Math.max(0, inventorySize());
        if (perChannel == 0) return 0;
        if (inventoryStyle() == InventoryStyle.BULK) {
            return Math.max(1, bulkChannels()) * perChannel;
        }
        return ((perChannel + 8) / 9) * 9;
    }

    /**
     * Created on first use, because the hooks above are overridden by subclasses and aren't
     * reliable during construction.
     */
    @Override
    public final SimpleContainer getVehicleInventory() {
        if (this.vehicleInventory == null) {
            this.vehicleInventory = new SimpleContainer(vehicleInventorySlots()) {
                @Override
                public boolean canPlaceItem(int slot, ItemStack stack) {
                    return CamoArtilleryBase.this.acceptsFromOutside(slot, stack);
                }

                /**
                 * Vanilla's addItem does NOT consult canPlaceItem — it just walks the whole
                 * container for the first empty slot and writes there. On a BULK hold that
                 * drops an item straight into a channel already holding something else,
                 * skipping the one-type rule entirely. Route it through the channel-aware
                 * path instead so every insert obeys the same rule.
                 */
                @Override
                public ItemStack addItem(ItemStack stack) {
                    if (CamoArtilleryBase.this.inventoryStyle() == InventoryStyle.BULK) {
                        return CamoArtilleryBase.this.storeItem(stack);
                    }
                    return super.addItem(stack);
                }
            };
        }
        return this.vehicleInventory;
    }

    /**
     * Route the vehicle's ITEM_HANDLER capability to our own cargo/ammo container. SBW's
     * ammo pipeline resolves ammo through ammoSupplier.getCapability(ITEM_HANDLER); pointing
     * that at getVehicleInventory() means the gun sees exactly what the player loads in the
     * GUI. Any other capability (energy, etc.) falls through to SBW.
     */
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && hasVehicleInventory()) {
            if (!ammoItemHandler.isPresent()) {
                ammoItemHandler = LazyOptional.of(() -> new InvWrapper(getVehicleInventory()));
            }
            return ammoItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        ammoItemHandler.invalidate();
    }

    @Override
    public void tick() {
        super.tick();
        chargeFromInventoryEnergyItems();
    }

    /**
     * Reimplements base SBW's behaviour: every 20 ticks, pull FE from any energy-bearing item
     * in the hold into the vehicle's battery — so a creative charging station (an infinite
     * energy source item) dropped into the inventory charges the vehicle for you. SBW's own
     * loop scans its native container, which FCP vehicles don't use, so we run the same scan
     * over getVehicleInventory(). Server-side, energy vehicles only.
     */
    private void chargeFromInventoryEnergyItems() {
        if (this.level().isClientSide() || !hasEnergyStorage() || !hasVehicleInventory()) return;
        if (this.tickCount % 20 != 0) return;

        SimpleContainer inventory = getVehicleInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int needed = getMaxEnergy() - getEnergy();
            if (needed <= 0) break;

            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            IEnergyStorage source = stack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
            if (source == null || source.getEnergyStored() <= 0) continue;

            int extracted = source.extractEnergy(Math.min(source.getEnergyStored(), needed), false);
            if (extracted > 0) setEnergy(getEnergy() + extracted);
        }
    }

    /**
     * The one rule deciding what may come in from OUTSIDE — hoppers land here, and the menu
     * applies the same test to its slots, so players and automation can never diverge.
     * The vehicle's own storeItem() bypasses all of this on purpose.
     */
    public boolean acceptsFromOutside(int slot, ItemStack stack) {
        if (!canPlaceIntoStorage()) return false;
        if (!canStoreItem(stack)) return false;
        if (inventoryStyle() != InventoryStyle.BULK) return true;

        // Bulk: a slot belongs to one channel, and a channel holds one type. An item that
        // already lives in another channel can't start a second one.
        int channel = slot / Math.max(1, bulkSlotsPerChannel());
        ItemStack type = bulkTypeOf(channel);
        if (!type.isEmpty()) return ItemStack.isSameItemSameTags(type, stack);
        return bulkChannelOf(stack) < 0;
    }

    /**
     * Open this vehicle's hold. The entity id and size are sent so the client can build a
     * matching menu and apply the same rules locally.
     */
    public void openVehicleInventory(ServerPlayer player) {
        // NONE has no storage but still shows a screen, so gate on the screen, not the hold.
        if (!opensVehicleScreen()) return;
        // Tidy any mixed channel before it's shown, so the player never looks at one.
        normalizeBulkChannels();
        SimpleContainer inventory = getVehicleInventory();
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider(
                        (id, playerInv, p) -> new VehicleInventoryMenu(id, playerInv, inventory, this,
                                inventoryStyle(), Math.max(1, bulkChannels())),
                        this.getDisplayName()),
                buf -> {
                    buf.writeVarInt(this.getId());
                    buf.writeVarInt(inventory.getContainerSize());
                    // Style and channel count decide the slot layout, so they must be known
                    // before the client builds its menu — a mismatched layout would desync
                    // every slot.
                    buf.writeVarInt(inventoryStyle().ordinal());
                    buf.writeVarInt(Math.max(1, bulkChannels()));
                });
    }

    // ── Render culling ──────────────────────────────────────────────────────────

    /**
     * Extra reach (blocks) for RENDER CULLING only — nothing else. Default 0 = untouched.
     *
     * EntityType.sized(w, h) can only describe a SQUARE footprint (w x h x w), so a vehicle
     * far wider than it is long has no hitbox that fits it: size it to the width and the
     * collision box becomes absurdly long; size it to the body and the model overhangs the
     * box. SBW's renderer frustum-tests boundingBoxForCulling inflated by 5, so an
     * overhanging model vanishes as soon as the centre nears the edge of the screen even
     * though half the vehicle is still on it.
     *
     * Set this to roughly half the model's LONGEST horizontal dimension. It's a single
     * radius rather than per-axis on purpose: the vehicle yaws, so a 19-wide implement is
     * 19 LONG once it turns 90 degrees, and an axis-aligned pad would break at some angles.
     *
     * Costs nothing but rendering the vehicle slightly more often than strictly needed.
     * Does NOT affect collision, hitboxes, OBBs or physics.
     */
    protected double renderCullPadding() {
        return 0.0;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        double padding = renderCullPadding();
        AABB box = super.getBoundingBoxForCulling();
        return padding <= 0.0 ? box : box.inflate(padding);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSq) {
        // Vanilla derives the render distance from the COLLISION box's size, so an
        // undersized hitbox also makes a big model pop out early. Use the padded box.
        if (renderCullPadding() <= 0.0) return super.shouldRenderAtSqrDistance(distanceSq);

        double size = getBoundingBoxForCulling().getSize();
        if (Double.isNaN(size)) size = 1.0;
        size = size * 64.0 * getViewScale();
        return distanceSq < size * size;
    }

    // ── Hold opening ────────────────────────────────────────────────────────────

    /**
     * True if this player is the one DRIVING (seat 0).
     *
     * Every other "who's the driver" check failed for a different reason:
     *   - getControllingPassenger() is null (SBW never overrides it);
     *   - getNthEntity(0) reads a private list populated SERVER-side only;
     *   - getTagSeatIndex() reads persistentData, which does NOT sync to the client.
     * The E-key handler runs client-side, so all three left isDriver wrong there and E kept
     * opening the player inventory.
     *
     * The vanilla passengers list, though, IS synced (ClientboundSetPassengersPacket) and SBW
     * fills it in seat order, so passenger 0 is the driver on both sides.
     */
    public boolean isDriver(Player player) {
        java.util.List<net.minecraft.world.entity.Entity> riders = this.getPassengers();
        return !riders.isEmpty() && riders.get(0) == player;
    }

    /**
     * The core "open the hold" interaction, run from interact() for EVERY vehicle. It only
     * consumes the click when it actually opens something, so mounting, crowbar, camo spray
     * and the rest still fall through to super untouched.
     *
     * Empty-handed, and only if there's a screen to show. Then:
     *   - DRIVING it — opens with no modifier; this is the path the E-key handler routes
     *     through so E opens the hold while seated (you can't re-mount, so no ambiguity);
     *   - standing beside a DRIVEABLE — needs SHIFT, because a plain click must still MOUNT;
     *   - standing beside a TRAILER — opens on a plain click, since you don't mount a
     *     trailer (AbstractTrailerEntity flips {@link #opensHoldOnPlainClick()} to true).
     *
     * @return a result to return from interact(), or null to fall through to super.
     */
    @Nullable
    protected InteractionResult openHoldInteraction(Player player, InteractionHand hand) {
        if (!player.getItemInHand(hand).isEmpty()) return null;
        if (!opensVehicleScreen()) return null;

        // A driveable opens ONLY on shift here. It must NOT open on a plain click even when
        // isDriver is momentarily true: the click that seats you evaluates interact() again
        // once you're in seat 0, and opening there is the "mounting also opens the hold"
        // bug. Riding-opens comes through openHoldForRider() instead, never this path.
        if (!opensHoldOnPlainClick() && !player.isShiftKeyDown()) {
            return null; // plain click on a driveable -> let super mount, don't open
        }

        if (this.level().isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            openVehicleInventory(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Open the hold for a player RIDING this vehicle — the inventory-redirect handler's entry
     * point, and the only way riding opens the hold. Kept separate from interact() so a plain
     * mount click can never trigger it (interact() never checks who's riding, so seating you
     * can't open the hold). Server-side only.
     *
     * Any occupant may open it, not just the driver: pinning this to seat 0 meant a check
     * that kept resolving wrong and blocked the feature outright, and a passenger reaching
     * the cargo is reasonable anyway.
     */
    public void openHoldForRider(ServerPlayer player) {
        if (!opensVehicleScreen()) return;
        if (player.getVehicle() != this && player.getRootVehicle() != this) return;
        openVehicleInventory(player);
    }

    /**
     * Whether a plain (non-shift) empty-handed click opens the hold. False for driveables
     * (a plain click mounts them); trailers override to true, since you don't mount one.
     */
    protected boolean opensHoldOnPlainClick() {
        return false;
    }

    /**
     * Spill the hold BEFORE the entity is serialised into the container item.
     *
     * The crowbar pickup path is: getRetrieveItems() -> ContainerBlockItem.createInstance(this)
     * -> entity.serializeNBT() (which captures our "VehicleInventory" tag), and only THEN
     * remove(DISCARDED). Since DISCARDED.shouldDestroy() is true, remove() also dropped the
     * contents - so every stack ended up BOTH inside the picked-up container item AND on the
     * ground: an item dupe. Dropping and clearing here means the NBT snapshot taken a moment
     * later sees an empty hold, so the cargo exists in exactly one place.
     *
     * Guarded to the server: getPickResult() (creative middle-click) also calls this, but only
     * client-side, so pick-block never spills a vehicle's cargo.
     */
    @Override
    public List<ItemStack> getRetrieveItems() {
        if (!this.level().isClientSide() && hasVehicleInventory()) {
            SimpleContainer inventory = getVehicleInventory();
            if (!inventory.isEmpty()) {
                Containers.dropContents(this.level(), this, inventory);
                // dropContents does NOT empty the container, so clear it explicitly - this is
                // what keeps the copy out of the container item's NBT.
                inventory.clearContent();
            }
        }
        return super.getRetrieveItems();
    }

    @Override
    public void remove(RemovalReason reason) {
        // Spill cargo when destroyed — but NOT on chunk unload.
        if (!this.level().isClientSide() && reason.shouldDestroy() && hasVehicleInventory()) {
            Containers.dropContents(this.level(), this, getVehicleInventory());
        }
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("CamoType", getCamoType());
        if (hasVehicleInventory()) {
            compound.put("VehicleInventory", getVehicleInventory().createTag());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("CamoType")) {
            setCamoType(compound.getInt("CamoType"));
        }
        if (hasVehicleInventory() && compound.contains("VehicleInventory", Tag.TAG_LIST)) {
            getVehicleInventory().fromTag(compound.getList("VehicleInventory", Tag.TAG_COMPOUND));
            // Saved contents were laid out against whatever the channel count and size were
            // at the time. If either has changed since, the old stacks now fall across
            // different boundaries and a channel can hold two types. Repair on load.
            normalizeBulkChannels();
        }
    }

    /**
     * Enforce "one item type per bulk channel" across the whole hold.
     *
     * The insert guards only vet items arriving; this fixes a hold that is ALREADY mixed —
     * which happens when bulkChannels() or inventorySize() changes between sessions, since
     * the saved stacks then get re-sliced against new channel boundaries.
     *
     * A channel's type is whatever its first non-empty slot holds. Anything else in that
     * channel is moved to the channel that already holds it, or to a free one. A stray with
     * nowhere to go is LEFT WHERE IT IS rather than deleted — a mixed channel is better than
     * silently eaten cargo, and it'll sort itself out once space frees up.
     */
    public void normalizeBulkChannels() {
        if (inventoryStyle() != InventoryStyle.BULK || !hasVehicleInventory()) return;

        SimpleContainer inventory = getVehicleInventory();
        int channels = Math.max(1, bulkChannels());
        int per = bulkSlotsPerChannel();
        if (per <= 0) return;

        for (int channel = 0; channel < channels; channel++) {
            int start = channel * per;
            int end = Math.min(start + per, inventory.getContainerSize());

            ItemStack type = ItemStack.EMPTY;
            for (int i = start; i < end; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) continue;

                if (type.isEmpty()) {
                    type = stack; // this channel's type is the first thing in it
                    continue;
                }
                if (ItemStack.isSameItemSameTags(type, stack)) continue;

                // A stray: belongs to some other channel.
                int target = bulkChannelOf(stack);
                if (target < 0) target = firstFreeBulkChannel();
                if (target < 0 || target == channel) continue; // nowhere better to put it

                ItemStack moving = stack.copy();
                inventory.setItem(i, ItemStack.EMPTY);
                ItemStack leftover = moveIntoChannel(target, moving);
                if (!leftover.isEmpty()) {
                    inventory.setItem(i, leftover); // put back whatever wouldn't fit
                }
            }
        }
        inventory.setChanged();
    }

    /** Push a stack into one specific channel; returns whatever didn't fit. */
    private ItemStack moveIntoChannel(int channel, ItemStack stack) {
        SimpleContainer inventory = getVehicleInventory();
        int per = bulkSlotsPerChannel();
        int start = channel * per;
        int end = Math.min(start + per, inventory.getContainerSize());

        for (int i = start; i < end && !stack.isEmpty(); i++) {
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
        return stack;
    }

    @Override
    public abstract ResourceLocation[] getCamoTextures();

    @Override
    public abstract String[] getCamoNames();
}