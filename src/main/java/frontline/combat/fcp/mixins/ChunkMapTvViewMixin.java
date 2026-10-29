package frontline.combat.fcp.mixins;

import frontline.combat.fcp.entity.vehicle.Himars.HimarsTvView;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * HIMARS TV guidance: while a player flies a TV missile, the chunks sent to them (and their chunk-cache centre) follow
 * the missile instead of the player - see HimarsTvView. Everyone else is unaffected.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapTvViewMixin {

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getBlockX()I"))
    private int fcp$viewBlockX(ServerPlayer player) {
        return HimarsTvView.viewOf(player).getBlockX();
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getBlockZ()I"))
    private int fcp$viewBlockZ(ServerPlayer player) {
        return HimarsTvView.viewOf(player).getBlockZ();
    }

    @Redirect(method = {"move", "updatePlayerPos"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"))
    private SectionPos fcp$viewSection(EntityAccess entity) {
        return entity instanceof ServerPlayer player ? SectionPos.of(HimarsTvView.viewOf(player)) : SectionPos.of(entity);
    }
}
