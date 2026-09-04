package frontline.combat.fcp.entity.vehicle.JohnDeere;

import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Towed cultivator. While hitched, tills a strip across its width as it travels, using
 * Forge's HOE_TILL tool action — so anything a hoe can till (vanilla or modded) works,
 * grass and dirt included. No inventory; hitching comes from AbstractTrailerEntity.
 */
public class CultivatorEntity extends AbstractTrailerEntity {

    private static final double ROW_HALF_WIDTH = 4.5;
    /** Lateral gap between tines. 1.0 = one block column each. */
    private static final double ROW_SPACING = 1.0;
    /** Where the row sits along the implement: local Z (+forward, -rear). */
    private static final double ROW_LOCAL_Z = 0.0;
    /** How far below the implement's base to hunt for ground. */
    private static final int SEARCH_DEPTH = 2;
    /** Distance travelled between tilled rows; also the interpolation step. */
    private static final double TILL_STEP = 0.5;
    /** Safety cap on interpolation steps in one tick. */
    private static final int MAX_STEPS = 8;
    /** Travel beyond this in one tick is a teleport, not driving — skip it. */
    private static final double TELEPORT_DISTANCE = 12.0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private double lastTillX = Double.NaN;
    private double lastTillZ = Double.NaN;

    public CultivatorEntity(EntityType<CultivatorEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected double renderCullPadding() {
        return 10.0;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar reg) {
        reg.add(new AnimationController<>(this, "base", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void baseTick() {
        super.baseTick();

        if (this.level().isClientSide()) return;

        if (!isAttached()) {
            lastTillX = Double.NaN;
            lastTillZ = Double.NaN;
            return;
        }

        if (Double.isNaN(lastTillX)) {
            lastTillX = this.getX();
            lastTillZ = this.getZ();
            return;
        }

        double dx = this.getX() - lastTillX;
        double dz = this.getZ() - lastTillZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > TELEPORT_DISTANCE) {
            lastTillX = this.getX();
            lastTillZ = this.getZ();
            return;
        }
        if (dist < TILL_STEP) return;

        int steps = (int) Math.min(MAX_STEPS, Math.ceil(dist / TILL_STEP));
        for (int s = 1; s <= steps; s++) {
            double t = (double) s / steps;
            tillRow(lastTillX + dx * t, lastTillZ + dz * t);
        }

        lastTillX = this.getX();
        lastTillZ = this.getZ();
    }

    private void tillRow(double cx, double cz) {
        int count = (int) Math.floor((ROW_HALF_WIDTH * 2.0) / ROW_SPACING) + 1;

        for (int i = 0; i < count; i++) {
            double lx = -ROW_HALF_WIDTH + i * ROW_SPACING;
            // Full body rotation (yaw AND pitch), so the tines work where the model shows
            // them on a slope rather than at flat-ground positions.
            var off = rotateBodyLocal(lx, 0, ROW_LOCAL_Z);
            tillAt(cx + off.x, this.getY() + off.y, cz + off.z);
        }
    }

    private void tillAt(double wx, double wy, double wz) {
        Level level = this.level();
        BlockPos base = BlockPos.containing(wx, wy, wz);

        if (!level.hasChunkAt(base)) return;

        for (int dy = 1; dy >= -SEARCH_DEPTH; dy--) {
            BlockPos pos = base.offset(0, dy, 0);
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            BlockState tilled = tilledState(state, pos);
            if (tilled == null) return;

            level.setBlock(pos, tilled, Block.UPDATE_ALL);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(this, tilled));
            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 0.5f, 1.0f);
            return;
        }
    }

    /** Null-player hoe context so getToolModifiedState resolves grass/dirt/path -> farmland. */
    private BlockState tilledState(BlockState state, BlockPos pos) {
        UseOnContext context = new TillContext(this.level(), new ItemStack(Items.DIAMOND_HOE),
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));

        return state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
    }

    private static class TillContext extends UseOnContext {
        TillContext(Level level, ItemStack tool, BlockHitResult hit) {
            super(level, (Player) null, InteractionHand.MAIN_HAND, tool, hit);
        }
    }
}