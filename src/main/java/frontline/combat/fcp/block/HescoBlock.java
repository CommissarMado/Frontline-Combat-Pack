package frontline.combat.fcp.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * HESCO barrier: a wire-mesh cage filled with sand or dirt. Falls like sand and gravel, and soaks up
 * blasts noticeably better than stone (see ModBlocks for its strength).
 */
public class HescoBlock extends FallingBlock {

    /** Colour of the dust particles that drip from it while it's unsupported. */
    private final int dustColor;

    public HescoBlock(int dustColor, Properties properties) {
        super(properties);
        this.dustColor = dustColor;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return dustColor;
    }
}
