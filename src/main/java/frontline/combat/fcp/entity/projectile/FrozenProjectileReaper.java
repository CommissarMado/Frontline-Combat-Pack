package frontline.combat.fcp.entity.projectile;

import frontline.combat.fcp.FCP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Deletes SBW / FCP projectiles that stop ticking after leaving the loaded area (user, 2026-11-03): a round in a chunk
 * that is loaded but not entity-ticking hangs in the air; once it hasn't ticked for {@link #FROZEN_SECONDS} it is
 * removed. A round whose chunk unloaded completely was saved frozen with it, so it is removed as soon as it is loaded
 * back from disk instead of resuming its flight (or blowing up) later.
 * Only SBW's and FCP's own projectiles - vanilla ones (a player's trident, arrows) are left alone. Artillery keeps its
 * chunks loaded in flight (ArtilleryChunkLoadMixin), so this only catches what flies off the edge.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class FrozenProjectileReaper {

    private static final int FROZEN_SECONDS = 5;
    private static final int CHECK_INTERVAL = 20; // ticks

    /** Per projectile: {tickCount at the last check, consecutive checks without a tick}. */
    private static final Map<Entity, int[]> SEEN = new WeakHashMap<>();
    private static int clock;

    private FrozenProjectileReaper() {
    }

    private static boolean ours(Entity entity) {
        if (!(entity instanceof Projectile)) return false;
        String name = entity.getClass().getName();
        return name.startsWith("com.atsuishio.superbwarfare.") || name.startsWith("frontline.combat.fcp.");
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !event.loadedFromDisk() || !ours(event.getEntity())) return;
        event.setCanceled(true); // saved while frozen in an unloading chunk: gone, not resumed
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++clock % CHECK_INTERVAL != 0) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!ours(entity) || entity.isRemoved()) continue;
                int[] seen = SEEN.get(entity);
                if (seen == null) {
                    SEEN.put(entity, new int[]{entity.tickCount, 0});
                    continue;
                }
                if (entity.tickCount != seen[0]) {
                    seen[0] = entity.tickCount;
                    seen[1] = 0;
                } else if (++seen[1] >= FROZEN_SECONDS * 20 / CHECK_INTERVAL) {
                    entity.discard();
                }
            }
        }
    }
}
