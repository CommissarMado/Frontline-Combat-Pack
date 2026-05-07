package frontline.combat.fcp.event;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * TrailerHitchHandler — handles right-click attach and detach interactions.
 *
 * Controls:
 *   Right-click trailer (empty hand or any item):
 *     - If already attached  → detach
 *     - If free              → find nearest SBW vehicle within HITCH_RADIUS and attach
 *
 *   Sneak + right-click trailer:
 *     - Force detach regardless of state (useful if tower is out of range)
 *
 * The search is performed around the TRAILER, not the player, so you can
 * right-click from a short distance after parking the towing vehicle next to it.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public class TrailerHitchHandler {

    /** Max distance in blocks from the trailer to search for a towing vehicle. */
    private static final double HITCH_RADIUS = 6.0;

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Only handle main hand to avoid double-firing
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        // Only care about trailers
        if (!(event.getTarget() instanceof AbstractTrailerEntity trailer)) return;

        // Server-side only — client will receive synced data updates
        if (event.getLevel().isClientSide()) return;

        // Consume the event so vanilla interaction doesn't also fire
        event.setCanceled(true);

        Player player = event.getEntity();

        // ── Sneak + right-click: force detach ────────────────────────────
        if (player.isShiftKeyDown()) {
            if (trailer.isAttached()) {
                trailer.detach();
                player.displayClientMessage(
                        Component.translatable("fcp.trailer.detached"), true);
            } else {
                player.displayClientMessage(
                        Component.translatable("fcp.trailer.not_attached"), true);
            }
            return;
        }

        // ── Already attached: detach ──────────────────────────────────────
        if (trailer.isAttached()) {
            trailer.detach();
            player.displayClientMessage(
                    Component.translatable("fcp.trailer.detached"), true);
            return;
        }

        // ── Free: find nearest valid towing vehicle and attach ────────────
        Entity tower = findNearestTower(trailer);

        if (tower == null) {
            player.displayClientMessage(
                    Component.translatable("fcp.trailer.no_vehicle_nearby"), true);
            return;
        }

        trailer.attachTo(tower);
        player.displayClientMessage(
                Component.translatable("fcp.trailer.attached"), true);
    }

    /**
     * Searches around the trailer for the nearest valid towing vehicle.
     * Valid means: a GeoVehicleEntity (SBW base), not a player, not another trailer.
     *
     * Returns null if nothing suitable is within HITCH_RADIUS.
     */
    @Nullable
    private static Entity findNearestTower(AbstractTrailerEntity trailer) {
        AABB searchBox = trailer.getBoundingBox().inflate(HITCH_RADIUS);

        List<Entity> candidates = trailer.level().getEntities(
                trailer,
                searchBox,
                entity -> isValidTower(entity, trailer)
        );

        if (candidates.isEmpty()) return null;

        return candidates.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(trailer.position())))
                .orElse(null);
    }

    private static boolean isValidTower(Entity entity, AbstractTrailerEntity trailer) {
        if (entity == trailer) return false;
        if (entity instanceof Player) return false;
        if (entity instanceof AbstractTrailerEntity) return false; // no chaining (yet)

        // Only allow SBW vehicles as towing entities.
        // GeoVehicleEntity is SBW's base — no extra dependency needed since
        // SBW is already a required dependency for this mod.
        // If the import path is wrong, check your SBW sources jar for the exact package.
        return entity instanceof GeoVehicleEntity;
    }
}