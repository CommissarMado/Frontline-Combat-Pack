package frontline.combat.fcp.entity.vehicle.JohnDeere;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.entity.vehicle.SteerableVehicle;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
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
 * ── Farming on / off ─────────────────────────────────────────────────────────
 * It only reaps with farming on (FarmingVehicle: the ammo type of the driver's "Farming" weapon), which also holds it to
 * harvesting speed and spins the reel; with it off it drives at road speed and leaves crops alone.
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
public class CombineEntity extends CamoVehicleBase implements FarmingVehicle, SteerableVehicle {

    private static final EntityDataAccessor<Boolean> FARMING = SynchedEntityData.defineId(CombineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(CombineEntity.class, EntityDataSerializers.FLOAT);
    /** Harvesting speed (7 km/h) over the 30 km/h road speed. */
    private static final float FARM_POWER = 7f / 30f;
    private float prevSteeringAngle = 0f;

    /** Driver-seat weapon whose ammo type switches the unloading auger: 0 off, 1 on. */
    public static final String AUGER_WEAPON = "Auger";
    private static final EntityDataAccessor<Boolean> AUGER_ON = SynchedEntityData.defineId(CombineEntity.class, EntityDataSerializers.BOOLEAN);
    /** Spout swing times: the lengths of the Bradley TOW launcher's up / down sounds (bradley_TOW_up/down_ext_Cue). */
    private static final float AUGER_OUT_TICKS = 6.30f * 20f, AUGER_IN_TICKS = 5.66f * 20f;
    private static final SoundEvent AUGER_UP = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FCP.MODID, "squad.bradley.tow.up_ext"));
    private static final SoundEvent AUGER_DOWN = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FCP.MODID, "squad.bradley.tow.down_ext"));
    /**
     * Spout tip with the spout swung out (vehicle config space: x left, z forward). The "spout" bone pivots at (-1.3033,
     * 4.1699, -0.1506) and its tip (end of the 7.5 degree nozzle) sits 7.437 behind it and 0.228 lower; swung out 50 degrees
     * to the right that puts it at pivot + (-7.437 sin 50, -0.228, -7.437 cos 50).
     */
    private static final double[] SPOUT_TIP = {-7.0004, 3.9419, -4.9310};
    private float augerProgress, augerProgressO;
    private boolean augerWasOn;

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
    /** Blocks searched below and above each header point, so the header works over rough ground. */
    private static final int SEARCH_DEPTH = 3, SEARCH_UP = 2;
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
    public void registerControllers(AnimatableManager.ControllerRegistrar reg) {
        reg.add(new AnimationController<>(this, "base", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ── Road / Farm mode, steering ──────────────────────────────────────────────

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FARMING, false);
        builder.define(STEERING_ANGLE, 0f);
        builder.define(AUGER_ON, false);
    }

    @Override
    public boolean isFarming() {
        return this.entityData.get(FARMING);
    }

    @Override
    public float farmPowerCap() {
        return FARM_POWER;
    }

    @Override
    public float getSteeringAngle() {
        return this.entityData.get(STEERING_ANGLE);
    }

    @Override
    public float getPrevSteeringAngle() {
        return prevSteeringAngle;
    }

    /** Steering input for the wheel animation only (CombineModel: rear wheels opposite, header wheels with it); SBW turns the combine. */
    private void updateSteering() {
        prevSteeringAngle = getSteeringAngle();
        float angle = getSteeringAngle();
        boolean left = this.leftInputDown(), right = this.rightInputDown();
        if (left && !right) angle = Math.min(30f, angle + 2f);
        else if (right && !left) angle = Math.max(-30f, angle - 2f);
        else angle *= 0.85f;
        if (Math.abs(angle) < 0.05f) angle = 0f;
        this.entityData.set(STEERING_ANGLE, angle);
    }

    // ── Unloading auger ─────────────────────────────────────────────────────────

    /** Is the auger switched on (synced copy of the driver's "Auger" weapon ammo type)? */
    public boolean isAugerOn() {
        return this.entityData.get(AUGER_ON);
    }

    /** How far the spout has swung out (0 folded .. 1 out), eased like a powered drive, for the model. */
    public float augerOut(float partialTick) {
        float t = Mth.lerp(partialTick, augerProgressO, augerProgress);
        return t * t * (3f - 2f * t);
    }

    /**
     * Swings the spout out or in at the Bradley TOW launcher's up / down sound lengths (that sound plays with it), on both
     * sides from the synced switch; once fully out, empties the hold out of the spout tip, one stack a tick (server).
     */
    private void tickAuger() {
        boolean on = isAugerOn();
        if (this.level().isClientSide() && on != augerWasOn && this.tickCount > 1) {
            this.level().playLocalSound(this.getX(), this.getY() + 3, this.getZ(), on ? AUGER_UP : AUGER_DOWN, SoundSource.NEUTRAL, 1f, 1f, false);
        }
        augerWasOn = on;
        augerProgressO = augerProgress;
        augerProgress = on ? Math.min(1f, augerProgress + 1f / AUGER_OUT_TICKS) : Math.max(0f, augerProgress - 1f / AUGER_IN_TICKS);
        if (this.level().isClientSide() || !on || augerProgress < 1f) return;

        SimpleContainer hold = getVehicleInventory();
        for (int slot = 0; slot < hold.getContainerSize(); slot++) {
            ItemStack stack = hold.getItem(slot);
            if (stack.isEmpty()) continue;
            org.joml.Vector4d tip = this.transformPosition(this.getVehicleTransform(1f), SPOUT_TIP[0], SPOUT_TIP[1], SPOUT_TIP[2]);
            ItemEntity item = new ItemEntity(this.level(), tip.x, tip.y, tip.z, hold.removeItemNoUpdate(slot));
            item.setDeltaMovement(this.getDeltaMovement().x, -0.1, this.getDeltaMovement().z);
            item.setDefaultPickUpDelay();
            this.level().addFreshEntity(item);
            hold.setChanged();
            return;
        }
    }

    // ── Tick / harvesting ───────────────────────────────────────────────────────

    @Override
    public void baseTick() {
        super.baseTick();
        updateSteering();
        if (!this.level().isClientSide()) {
            this.entityData.set(FARMING, FarmingVehicle.switchedOn(this));
            this.entityData.set(AUGER_ON, FarmingVehicle.switchedOn(this, AUGER_WEAPON));
        }
        FarmingVehicle.capPower(this);
        tickAuger();

        if (this.level().isClientSide()) return;

        // Farming off: no reaping, and the next working stretch starts where the combine is.
        if (!isFarming() || Double.isNaN(lastReapX)) {
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
        // The header is well ahead of the body: follow the body's pitch (xRot positive = nose down) so it reaps where it is
        // on a slope, not at the body's height.
        double pitch = Math.toRadians(this.getXRot());
        double lz = HEADER_FORWARD * Math.cos(pitch); // reap in front of the body
        double ly = -HEADER_FORWARD * Math.sin(pitch);

        for (int i = 0; i < count; i++) {
            double lx = -ROW_HALF_WIDTH + i * ROW_SPACING;
            double wx = cx + (lx * cos - lz * sin);
            double wz = cz + (lx * sin + lz * cos);
            reapAt(wx, this.getY() + ly, wz);
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

        for (int dy = SEARCH_UP; dy >= -SEARCH_DEPTH; dy--) {
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