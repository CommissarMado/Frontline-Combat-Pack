package frontline.combat.fcp.mixins;

import frontline.combat.fcp.entity.vehicle.Himars.HimarsTvView;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * HIMARS TV guidance: while a player flies a TV missile, the chunks sent to them (and their chunk-cache centre) follow
 * the missile instead of the player - see HimarsTvView. Everyone else is unaffected.
 * <p>
 * Lithium and Radium overwrite move at the default priority 1000, and Mixin refuses to inject into that at the same priority or
 * lower, so this goes after them.
 */
@Mixin(value = ChunkMap.class, priority = 1100)
public abstract class ChunkMapTvViewMixin {

    /** The centre of the chunk view sent to the player (1.20.2+ tracks it as a ChunkTrackingView). */
    @Redirect(method = "updateChunkTracking", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;chunkPosition()Lnet/minecraft/world/level/ChunkPos;"))
    private ChunkPos fcp$viewChunk(ServerPlayer player) {
        return SectionPos.of(HimarsTvView.viewOf(player)).chunk();
    }

    @Redirect(method = {"move", "updatePlayerPos"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"))
    private SectionPos fcp$viewSection(EntityAccess entity) {
        return entity instanceof ServerPlayer player ? SectionPos.of(HimarsTvView.viewOf(player)) : SectionPos.of(entity);
    }
}
