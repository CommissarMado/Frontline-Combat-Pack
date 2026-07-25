package frontline.combat.fcp.entity.vehicle.JohnDeere;

import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * CombineEntity — a driveable combine harvester (NOT a trailer; it extends CamoVehicleBase
 * and is driven directly). As it drives it reaps mature crops across its width and stores
 * the whole yield in a large scrollable inventory — produce and seed alike.
 *
 * It does NOT replant: harvesting is all it does, and the seed drill handles sowing. A
 * reaped block is left as bare farmland, ready for the seeder to follow.
 *
 * ── Harvesting ───────────────────────────────────────────────────────────────
 * Each tick it stamps a ROW of headers across its width, ahead of and perpendicular to its
 * heading (its own getYRot(), since it steers itself). Every header looks a short way DOWN
 * for a fully grown {@link CropBlock} and, if it finds one:
 *   1. takes the block's real loot ({@code getDrops}) — seeds and produce both,
 *   2. clears the block,
 *   3. puts the entire yield into the inventory.
 *
 * Working off the block's loot table means modded crops harvest and drop correctly for free,
 * exactly as the seed drill plants any IPlantable. Only fully grown crops are touched, so
 * anything still growing is left alone — nothing to track.
 *
 * ── Header offset ────────────────────────────────────────────────────────────
 * A real combine reaps in FRONT of itself. HEADER_FORWARD pushes the reaping row ahead of
 * the body so it cuts crops before the vehicle drives over them; 0 reaps under the centre.
 *
 * ── Storage ──────────────────────────────────────────────────────────────────
 * A plain GRID inventory (inventorySize slots). If the whole yield won't fit, the crop is
 * left standing so nothing is voided, and the combine simply stops reaping until emptied.
 * It's an ordinary container: mount and drive normally, and open the hold the same way you'd
 * open any FCP vehicle's storage.
 *
 * Stem crops (pumpkin/melon gourds) aren't CropBlocks and are left for a later pass.
 */
public class CombineEntity extends CamoVehicleBase {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            // Normal
            new ResourceLocation("fcp", "textures/entity/tractor/combine.png"),
            // Wrecked
            new ResourceLocation("fcp", "textures/entity/tractor/combine_wrecked.png")
    };

    private static final String[] CAMO_NAMES = {"John Deere"};

    // ── Cargo ───────────────────────────────────────────────────────────────────
    /** Yield inventory size in slots. Rounded up to a multiple of 9; scrolls past 63. */

    private static final int INVENTORY_CAPACITY = 306000;
    private static final int INVENTORY_SIZE = INVENTORY_CAPACITY / 64; // 12 rows -> 5 rows scroll past the 7 shown

    // ── Header geometry / tuning ────────────────────────────────────────────────
    /** 9 at 1.0 spacing = 19 headers = a 19-block-wide combine. */
    private static final double ROW_HALF_WIDTH = 3;
    private static final double ROW_SPACING = 1.0;
    /** How far in FRONT of the body the header reaps (+forward). */
    private static final double HEADER_FORWARD = 6.0;
    private static final int SEARCH_DEPTH = 2;
    private static final double REAP_STEP = 0.5;
    private static final int MAX_STEPS = 8;
    private static final double TELEPORT_DISTANCE = 12.0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** Last position a row was stamped at; NaN until harvesting starts. */
    private double lastReapX = Double.NaN;
    private double lastReapZ = Double.NaN;

    public CombineEntity(EntityType<CombineEntity> type, Level world) {
        super(type, world);
    }

    /**
     * The header is ~19 blocks wide but only a few long, so no square hitbox fits it. Pad
     * the render bounds to about half the width, otherwise it culls off the edge of the
     * screen while still visibly on it. Render-only — collision is unaffected.
     */
    @Override
    protected double renderCullPadding() {
        return 12.0;
    }

    // ── Cargo: one large scrollable grid ────────────────────────────────────────

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override
    public InventoryStyle inventoryStyle() {
        return InventoryStyle.BULK;
    }

    @Override
    public int bulkChannels() {
        return 6;
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

    // ── Tick / harvesting ───────────────────────────────────────────────────────

    @Override
    public void baseTick() {
        super.baseTick();

        if (this.level().isClientSide()) return;

        if (Double.isNaN(lastReapX)) {
            lastReapX = this.getX();
            lastReapZ = this.getZ();
            return;
        }

        double dx = this.getX() - lastReapX;
        double dz = this.getZ() - lastReapZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > TELEPORT_DISTANCE) { // jumped somewhere — don't reap a strip across the map
            lastReapX = this.getX();
            lastReapZ = this.getZ();
            return;
        }
        if (dist < REAP_STEP) return; // not enough travel yet

        // Stamp rows along the path actually covered, so speed can't leave gaps. Interpolate
        // yaw too: on a 19-wide header the outer teeth swing a long way through a turn, and
        // reusing the final yaw for every step would smear the reaped rows sideways.
        int steps = (int) Math.min(MAX_STEPS, Math.ceil(dist / REAP_STEP));
        float yaw = this.getYRot();
        float prevYaw = this.yRotO;
        for (int s = 1; s <= steps; s++) {
            float t = (float) s / steps;
            reapRow(lastReapX + dx * t, lastReapZ + dz * t,
                    net.minecraft.util.Mth.rotLerp(t, prevYaw, yaw));
        }

        lastReapX = this.getX();
        lastReapZ = this.getZ();
    }

    /** Stamp one row of headers across the width, centred on (cx, cz) at the given yaw. */
    private void reapRow(double cx, double cz, float yaw) {
        int count = (int) Math.floor((ROW_HALF_WIDTH * 2.0) / ROW_SPACING) + 1;

        // x = right, z = forward, yaw about the vehicle position — the convention used
        // across the farm implements.
        double theta = Math.toRadians(yaw);
        double cos = Math.cos(theta), sin = Math.sin(theta);

        for (int i = 0; i < count; i++) {
            double lx = -ROW_HALF_WIDTH + i * ROW_SPACING;
            double lz = HEADER_FORWARD; // reap in front of the body
            double wx = cx + (lx * cos - lz * sin);
            double wz = cz + (lx * sin + lz * cos);
            reapAt(wx, this.getY(), wz);
        }
    }

    /** Find a mature crop at/below this column and harvest it. */
    private void reapAt(double wx, double wy, double wz) {
        Level level = this.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        BlockPos base = BlockPos.containing(wx, wy, wz);

        // A wide header reaches well past the vehicle itself and can overhang chunks that
        // aren't loaded; touching those would force-load them. Skip instead.
        if (!level.hasChunkAt(base)) return;

        for (int dy = 1; dy >= -SEARCH_DEPTH; dy--) {
            BlockPos pos = base.offset(0, dy, 0);
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof CropBlock crop)) continue;
            if (!crop.isMaxAge(state)) return; // a crop here, just not ready — leave it, done

            harvest(serverLevel, crop, state, pos);
            return; // one header reaps one block
        }
    }

    /**
     * Reap one mature crop: pull its real drops, clear the block, and store the lot. If the
     * inventory can't take it all, the crop is left standing so nothing is lost.
     */
    private void harvest(ServerLevel level, CropBlock crop, BlockState state, BlockPos pos) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos));

        // Don't reap into an inventory that can't hold the yield — check before breaking
        // anything, so a full combine leaves the crop standing instead of voiding it.
        if (!wouldFit(drops)) return;

        // Harvest only: clear the crop and keep the whole yield, seeds included. Putting a
        // seed back is the seeder's job, so the ground is left bare (still tilled farmland)
        // ready for it.
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        // Store everything straight into the inventory.
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            ItemStack leftover = getVehicleInventory().addItem(drop);
            if (!leftover.isEmpty()) {
                // Shouldn't happen after wouldFit, but never delete produce — drop it.
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, leftover);
            }
        }

        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(this, state));
        level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.5f, 1.0f);
    }

    /**
     * Would the whole yield fit? Simulated against a scratch copy of the inventory so we
     * never half-reap. SimpleContainer.addItem does the same stacking a real insert would,
     * so the trial matches reality.
     */
    private boolean wouldFit(List<ItemStack> drops) {
        SimpleContainer scratch = new SimpleContainer(getVehicleInventory().getContainerSize());
        for (int i = 0; i < scratch.getContainerSize(); i++) {
            scratch.setItem(i, getVehicleInventory().getItem(i).copy());
        }

        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            ItemStack leftover = scratch.addItem(drop.copy());
            if (!leftover.isEmpty()) return false; // didn't all fit
        }
        return true;
    }

}