package frontline.combat.fcp.entity.vehicle.JohnDeere;

import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * SeederEntity — a towed seed drill. While being towed it plants seeds from its own
 * inventory onto any farmland it passes over.
 *
 * ── Planting ─────────────────────────────────────────────────────────────────
 * Each tick it stamps a ROW of coulters across its width, perpendicular to heading.
 * Every coulter independently:
 *   1. looks a short way DOWN for farmland (works over uneven ground),
 *   2. checks the block above that farmland is air,
 *   3. places the first inventory seed the farmland can actually sustain.
 *
 * The air check makes this idempotent — driving the same strip twice never replants
 * or wastes seeds, so no visited-block tracking is needed.
 *
 * Travel is INTERPOLATED: at speed the trailer covers more than a block per tick, so
 * planting only at the current position would leave gaps. Rows are stamped at
 * intervals along the path actually covered since the last row.
 *
 * Seeds are matched with Forge's IPlantable + canSustainPlant, so vanilla crops
 * (wheat/carrot/potato/beetroot) and modded crops work with no item list to maintain.
 *
 * ── Interaction ──────────────────────────────────────────────────────────────
 *   empty hand on the TONGUE  -> hitch / unhitch  (handled by AbstractTrailerEntity)
 *   empty hand on the BODY    -> open the seed hopper
 *   any item in hand          -> that item's own behaviour (crowbar pickup, spray, ...)
 */
public class SeederEntity extends AbstractTrailerEntity {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            // Normal
            new ResourceLocation("fcp", "textures/entity/tractor/john_deere.png"),
            // Wrecked
            new ResourceLocation("fcp", "textures/entity/tractor/john_deere_wrecked.png")
    };

    private static final String[] CAMO_NAMES = {"John Deere"};

    // ── Seeder geometry / tuning ────────────────────────────────────────────────
    /** Inventory slots. Must be a multiple of 9 (uses the vanilla chest UI). */
    private static final int INVENTORY_SIZE = 27;
    /** Half the planting width in blocks. 1.5 => a 4-wide row at 1.0 spacing. */
    private static final double ROW_HALF_WIDTH = 9;
    /** Lateral gap between coulters. 1.0 = one seed per block column. */
    private static final double ROW_SPACING = 1.0;
    /** Where the row sits along the trailer: local Z (+forward, -rear). */
    private static final double ROW_LOCAL_Z = 0.0;
    /** How far below the trailer's base to hunt for farmland. */
    private static final int SEARCH_DEPTH = 2;
    /** Distance travelled between planted rows; also the interpolation step. */
    private static final double PLANT_STEP = 0.5;
    /** Safety cap on interpolation steps in one tick. */
    private static final int MAX_STEPS = 8;
    /** Travel beyond this in one tick is a teleport, not driving — skip it. */
    private static final double TELEPORT_DISTANCE = 12.0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

    /** Last position a row was stamped at; NaN until seeding starts. */
    private double lastSeedX = Double.NaN;
    private double lastSeedZ = Double.NaN;

    public SeederEntity(EntityType<SeederEntity> type, Level world) {
        super(type, world);
    }

    // ── Vehicle plumbing ────────────────────────────────────────────────────────

    @Override
    public ResourceLocation[] getCamoTextures() {
        return CAMO_TEXTURES;
    }

    @Override
    public String[] getCamoNames() {
        return CAMO_NAMES;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar reg) {
        reg.add(new AnimationController<>(this, "base", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ── Tick / seeding ──────────────────────────────────────────────────────────

    @Override
    public void baseTick() {
        super.baseTick();

        if (this.level().isClientSide()) return;

        // Only a towed drill plants.
        if (!isAttached()) {
            lastSeedX = Double.NaN; // resume cleanly on the next hitch
            lastSeedZ = Double.NaN;
            return;
        }

        if (Double.isNaN(lastSeedX)) {
            lastSeedX = this.getX();
            lastSeedZ = this.getZ();
            return;
        }

        double dx = this.getX() - lastSeedX;
        double dz = this.getZ() - lastSeedZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > TELEPORT_DISTANCE) { // jumped somewhere — don't stripe the world
            lastSeedX = this.getX();
            lastSeedZ = this.getZ();
            return;
        }
        if (dist < PLANT_STEP) return; // not enough travel yet

        // Stamp rows along the path actually covered, so speed can't leave gaps.
        int steps = (int) Math.min(MAX_STEPS, Math.ceil(dist / PLANT_STEP));
        for (int s = 1; s <= steps; s++) {
            double t = (double) s / steps;
            plantRow(lastSeedX + dx * t, lastSeedZ + dz * t);
        }

        lastSeedX = this.getX();
        lastSeedZ = this.getZ();
    }

    /** Stamp one row of coulters across the width, centred on (cx, cz). */
    private void plantRow(double cx, double cz) {
        int count = (int) Math.floor((ROW_HALF_WIDTH * 2.0) / ROW_SPACING) + 1;

        // Yaw-only rotation about the trailer position — the same convention the hitch
        // constraint uses (x = right, z = forward).
        double theta = Math.toRadians(this.getYRot());
        double cos = Math.cos(theta), sin = Math.sin(theta);

        for (int i = 0; i < count; i++) {
            double lx = -ROW_HALF_WIDTH + i * ROW_SPACING;
            double wx = cx + (lx * cos - ROW_LOCAL_Z * sin);
            double wz = cz + (lx * sin + ROW_LOCAL_Z * cos);
            plantAt(wx, this.getY(), wz);
        }
    }

    /** Find farmland at/below this column and plant one seed on it. */
    private void plantAt(double wx, double wy, double wz) {
        Level level = this.level();
        BlockPos base = BlockPos.containing(wx, wy, wz);

        for (int dy = 1; dy >= -SEARCH_DEPTH; dy--) {
            BlockPos soil = base.offset(0, dy, 0);
            BlockState soilState = level.getBlockState(soil);
            if (!soilState.is(Blocks.FARMLAND)) continue;

            BlockPos cropPos = soil.above();
            // Occupied (already planted, or something in the way) — leave it alone.
            if (!level.getBlockState(cropPos).isAir()) return;

            tryPlant(soilState, soil, cropPos);
            return; // first farmland found is the one we seed
        }
    }

    /** Place the first inventory seed this soil can sustain. */
    private boolean tryPlant(BlockState soilState, BlockPos soil, BlockPos cropPos) {
        Level level = this.level();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof BlockItem blockItem)) continue;

            Block block = blockItem.getBlock();
            if (!(block instanceof IPlantable plantable)) continue;
            if (!soilState.canSustainPlant(level, soil, Direction.UP, plantable)) continue;

            BlockState crop = block.defaultBlockState();
            if (!crop.canSurvive(level, cropPos)) continue;

            level.setBlock(cropPos, crop, Block.UPDATE_ALL);
            level.gameEvent(GameEvent.BLOCK_PLACE, cropPos, GameEvent.Context.of(this, crop));
            level.playSound(null, cropPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.5f, 1.0f);

            stack.shrink(1);
            if (stack.isEmpty()) inventory.setItem(slot, ItemStack.EMPTY);
            inventory.setChanged();
            return true;
        }
        return false;
    }

    // ── Inventory ───────────────────────────────────────────────────────────────

    /**
     * Empty hand on the body opens the hopper. Hitching (empty hand on the tongue) is
     * consumed earlier by AbstractTrailerEntity.interactAt, and a held item falls through
     * to the normal vehicle behaviour so the crowbar and camo spray still work.
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.getItemInHand(hand).isEmpty()) {
            return super.interact(player, hand);
        }
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (id, playerInv, p) -> ChestMenu.threeRows(id, playerInv, this.inventory),
                    this.getDisplayName()));
        }
        return InteractionResult.SUCCESS;
    }

    // ── Persistence / cleanup ───────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("SeedInventory", inventory.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SeedInventory", Tag.TAG_LIST)) {
            inventory.fromTag(tag.getList("SeedInventory", Tag.TAG_COMPOUND));
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        // Spill the seeds when destroyed — but NOT on chunk unload.
        if (!this.level().isClientSide() && reason.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this.inventory);
        }
        super.remove(reason);
    }
}