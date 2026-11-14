package frontline.combat.fcp.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * HESCO barrier: a wire-mesh cage filled with sand or dirt. Falls like sand and gravel, and soaks up
 * blasts noticeably better than stone (see ModBlocks for its strength).
 */
public class HescoBlock extends FallingBlock {

    public static final MapCodec<HescoBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("dust_color").forGetter(block -> block.dustColor),
            propertiesCodec()
    ).apply(instance, HescoBlock::new));

    /** Colour of the dust particles that drip from it while it's unsupported. */
    private final int dustColor;

    public HescoBlock(int dustColor, Properties properties) {
        super(properties);
        this.dustColor = dustColor;
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return dustColor;
    }
}
