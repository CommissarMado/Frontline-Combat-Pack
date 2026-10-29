package frontline.combat.fcp.mixins;

import net.minecraft.server.level.ServerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** A tracked entity's ServerEntity (which sends it to players), for HimarsTvView. */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface TrackedEntityAccessor {

    @Accessor("serverEntity")
    ServerEntity fcp$serverEntity();
}
