package frontline.combat.fcp.entity.vehicle.Himars;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity;
import frontline.combat.fcp.mixins.ChunkMapAccessor;
import frontline.combat.fcp.mixins.TrackedEntityAccessor;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Server side of long-range HIMARS TV guidance. A player can only see chunks and entities around one centre, which is
 * normally themselves. While they fly a TV missile, that centre is the missile instead (ChunkMapTvViewMixin and
 * TrackedEntityTvViewMixin read {@link #viewOf}), so the picture keeps coming however far it flies. Their own vehicle
 * stays tracked, and a chunk ticket keeps it (with them in it) loaded on the server.
 * <p>
 * Seated players normally re-centre on their own movement packets, but a client whose own chunk has gone stops
 * sending them, so the re-centring is driven from here every tick of the flight, and once more when it ends. On the
 * way back the player's vehicle and crew are re-sent to them too, so a client that lost track of them while their
 * chunks were away (HimarsEntity holds still meanwhile) gets them back, seated, where the server has them.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class HimarsTvView {

    /** Keeps the launcher's chunk entity-ticking while its gunner looks through a TV missile far away. */
    private static final TicketType<ChunkPos> LAUNCHER_TICKET =
            TicketType.create("fcp_himars_tv", Comparator.comparingLong(ChunkPos::toLong), 40);

    /** Players whose view is (as of last tick) out at a TV missile. */
    private static final Set<UUID> VIEWING = new HashSet<>();

    private HimarsTvView() {
    }

    /** The TV missile {@code player} is flying, or null. */
    public static HimarsMissileEntity missileOf(ServerPlayer player) {
        return player.getVehicle() instanceof HimarsEntity himars ? himars.tvMissileOf(player) : null;
    }

    /** What {@code player}'s chunks and entity tracking centre on: their TV missile while they fly one, else them. */
    public static Entity viewOf(ServerPlayer player) {
        HimarsMissileEntity missile = missileOf(player);
        return missile != null ? missile : player;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        HimarsMissileEntity missile = missileOf(player);
        if (player.tickCount % 10 == 0 && (missile != null || VIEWING.contains(player.getUUID())
                || player.getVehicle() instanceof HimarsEntity)) { // TEMPORARY diagnostics
            com.mojang.logging.LogUtils.getLogger().info("[HIMARS-DBG] SERVER view t={} missile={} missileXZ={} lastSection={} playerSection={}",
                    player.tickCount, missile == null ? "none" : missile.getId(),
                    missile == null ? "-" : String.format("%.0f,%.0f", missile.getX(), missile.getZ()),
                    player.getLastSectionPos(), SectionPos.of(player));
        }
        if (missile != null) {
            ChunkPos home = player.getRootVehicle().chunkPosition();
            player.serverLevel().getChunkSource().addRegionTicket(LAUNCHER_TICKET, home, 3, home);
            player.serverLevel().getChunkSource().move(player);
            VIEWING.add(player.getUUID());
        } else if (VIEWING.remove(player.getUUID())) {
            com.mojang.logging.LogUtils.getLogger().info("[HIMARS-DBG] SERVER view home: player y={} vehicle={} vehicleY={}", String.format("%.2f", player.getY()), player.getVehicle() == null ? "none" : player.getVehicle().getId(), player.getVehicle() == null ? "-" : String.format("%.2f", player.getVehicle().getY())); // TEMPORARY diagnostics
            player.serverLevel().getChunkSource().move(player);
            resendVehicle(player);
        } else if (!player.getLastSectionPos().equals(SectionPos.of(player))) {
            // Flight over (or the launcher gone): bring the view home.
            player.serverLevel().getChunkSource().move(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        VIEWING.remove(event.getEntity().getUUID());
    }

    /** Re-sends {@code player}'s vehicle and the rest of its crew to them: removed, then added back seated. */
    private static void resendVehicle(ServerPlayer player) {
        Entity root = player.getRootVehicle();
        if (root == player) return;
        List<Entity> entities = new ArrayList<>();
        entities.add(root);
        for (Entity passenger : root.getIndirectPassengers()) {
            if (passenger != player) entities.add(passenger);
        }
        var trackers = ((ChunkMapAccessor) player.serverLevel().getChunkSource().chunkMap).fcp$entityMap();
        for (int i = entities.size() - 1; i >= 0; i--) {
            Object tracked = trackers.get(entities.get(i).getId());
            if (tracked != null) ((TrackedEntityAccessor) tracked).fcp$serverEntity().removePairing(player);
        }
        for (Entity entity : entities) {
            Object tracked = trackers.get(entity.getId());
            if (tracked != null) ((TrackedEntityAccessor) tracked).fcp$serverEntity().addPairing(player);
        }
    }
}
