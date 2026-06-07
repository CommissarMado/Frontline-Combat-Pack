package frontline.combat.fcp.event;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = FCP.MODID)
public class TrailerHitchHandler {
    private static final double HITCH_RADIUS = 6.0;

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getTarget() instanceof AbstractTrailerEntity trailer)) return;
        if (event.getLevel().isClientSide()) return;
        event.setCanceled(true);

        Player player = event.getEntity();

        if (player.isShiftKeyDown()) {
            if (trailer.isAttached()) { trailer.detach(); player.displayClientMessage(Component.translatable("fcp.trailer.detached"), true); }
            else { player.displayClientMessage(Component.translatable("fcp.trailer.not_attached"), true); }
            return;
        }

        if (trailer.isAttached()) {
            trailer.detach(); player.displayClientMessage(Component.translatable("fcp.trailer.detached"), true);
            return;
        }

        Entity tower = findNearestTower(trailer);
        if (tower == null) {
            player.displayClientMessage(Component.translatable("fcp.trailer.no_vehicle_nearby"), true);
            return;
        }
        trailer.attachTo(tower);
        player.displayClientMessage(Component.translatable("fcp.trailer.attached"), true);
    }

    @Nullable
    private static Entity findNearestTower(AbstractTrailerEntity trailer) {
        AABB searchBox = trailer.getBoundingBox().inflate(HITCH_RADIUS);
        List<Entity> candidates = trailer.level().getEntities(trailer, searchBox,
                entity -> entity instanceof GeoVehicleEntity && entity != trailer && !(entity instanceof Player) && !(entity instanceof AbstractTrailerEntity));
        if (candidates.isEmpty()) return null;
        return candidates.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(trailer.position()))).orElse(null);
    }
}
