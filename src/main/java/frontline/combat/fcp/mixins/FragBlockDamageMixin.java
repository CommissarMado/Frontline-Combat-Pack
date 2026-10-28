package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.tools.CustomExplosion;
import frontline.combat.fcp.balance.VehicleArmor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fragmentation rounds blow smaller craters than their blast damage would: blocks are kept only
 * within {@code VehicleArmor.blockScale} of the farthest block SBW would destroy. Entity damage is untouched.
 */
@Mixin(CustomExplosion.class)
public abstract class FragBlockDamageMixin {

    @ModifyVariable(method = "processBlockList", at = @At("HEAD"), argsOnly = true, remap = false)
    private Collection<BlockPos> fcp$shrinkFragCrater(Collection<BlockPos> blocks) {
        Explosion self = (Explosion) (Object) this;
        float scale = VehicleArmor.blockScale(self.getDirectSourceEntity());
        if (scale >= 1f || blocks.isEmpty()) return blocks;
        if (scale <= 0f) return List.of();
        CustomExplosion e = (CustomExplosion) (Object) this;
        double max = 0;
        for (BlockPos pos : blocks) max = Math.max(max, pos.distToCenterSqr(e.getX(), e.getY(), e.getZ()));
        double limit = max * scale * scale;
        List<BlockPos> kept = new ArrayList<>();
        for (BlockPos pos : blocks) if (pos.distToCenterSqr(e.getX(), e.getY(), e.getZ()) <= limit) kept.add(pos);
        return kept;
    }
}
