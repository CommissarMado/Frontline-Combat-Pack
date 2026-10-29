package frontline.combat.fcp.mixins;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The server's entity trackers (values are ChunkMap.TrackedEntity - see TrackedEntityAccessor), for HimarsTvView. */
@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {

    @Accessor("entityMap")
    Int2ObjectMap<Object> fcp$entityMap();
}
