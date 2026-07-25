package frontline.combat.fcp.entity.vehicle.JohnDeere;

import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import frontline.combat.fcp.entity.vehicle.VehicleInventory.InventoryStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.IPlantable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SeederEntity extends AbstractTrailerEntity {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            // Normal
            new ResourceLocation("fcp", "textures/entity/tractor/john_deere.png"),
            // Wrecked
            new ResourceLocation("fcp", "textures/entity/tractor/john_deere_wrecked.png")
    };

    private static final String[] CAMO_NAMES = {"John Deere"};
    private static final int SEED_CAPACITY = 30600;
    private static final int INVENTORY_SIZE = SEED_CAPACITY / 64;
    private static final double ROW_HALF_WIDTH = 9;
    private static final double ROW_SPACING = 1.0;
    private static final double ROW_LOCAL_Z = -4.0;
    private static final int SEARCH_DEPTH = 2;
    private static final double PLANT_STEP = 0.5;
    private static final int MAX_STEPS = 8;
    private static final double TELEPORT_DISTANCE = 12.0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private double lastSeedX = Double.NaN;
    private double lastSeedZ = Double.NaN;

    public SeederEntity(EntityType<SeederEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected double renderCullPadding() {
        return 10.0;
    }

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
        return 1;
    }

    @Override
    public boolean canStoreItem(ItemStack stack) {
        return isPlantableOnFarmland(this.level(), stack);
    }

    public static boolean isPlantableOnFarmland(BlockGetter level, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();
        if (!(block instanceof IPlantable plantable)) return false;
        return Blocks.FARMLAND.defaultBlockState()
                .canSustainPlant(level, BlockPos.ZERO, Direction.UP, plantable);
    }

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


    @Override
    public void baseTick() {
        super.baseTick();

        if (this.level().isClientSide()) return;

        if (!isAttached()) {
            lastSeedX = Double.NaN;
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

        if (dist > TELEPORT_DISTANCE) {
            lastSeedX = this.getX();
            lastSeedZ = this.getZ();
            return;
        }
        if (dist < PLANT_STEP) return;

        int steps = (int) Math.min(MAX_STEPS, Math.ceil(dist / PLANT_STEP));
        for (int s = 1; s <= steps; s++) {
            double t = (double) s / steps;
            plantRow(lastSeedX + dx * t, lastSeedZ + dz * t);
        }

        lastSeedX = this.getX();
        lastSeedZ = this.getZ();
    }

    private void plantRow(double cx, double cz) {
        int count = (int) Math.floor((ROW_HALF_WIDTH * 2.0) / ROW_SPACING) + 1;

        double theta = Math.toRadians(this.getYRot());
        double cos = Math.cos(theta), sin = Math.sin(theta);

        for (int i = 0; i < count; i++) {
            double lx = -ROW_HALF_WIDTH + i * ROW_SPACING;
            double wx = cx + (lx * cos - ROW_LOCAL_Z * sin);
            double wz = cz + (lx * sin + ROW_LOCAL_Z * cos);
            plantAt(wx, this.getY(), wz);
        }
    }

    private void plantAt(double wx, double wy, double wz) {
        Level level = this.level();
        BlockPos base = BlockPos.containing(wx, wy, wz);

        for (int dy = 1; dy >= -SEARCH_DEPTH; dy--) {
            BlockPos soil = base.offset(0, dy, 0);
            BlockState soilState = level.getBlockState(soil);
            if (!soilState.is(Blocks.FARMLAND)) continue;

            BlockPos cropPos = soil.above();
            if (!level.getBlockState(cropPos).isAir()) return;

            tryPlant(soilState, soil, cropPos);
        }
    }

    private boolean tryPlant(BlockState soilState, BlockPos soil, BlockPos cropPos) {
        Level level = this.level();

        SimpleContainer inventory = getVehicleInventory();
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

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.getItemInHand(hand).isEmpty()) {
            return super.interact(player, hand);
        }
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            openVehicleInventory(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

}