package frontline.combat.fcp.event;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = FCP.MODID)
public class TrailerEventHandler {
    private static final double SEARCH_RADIUS = 24.0;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) { detachTrailersFor(event.getEntity()); }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) { detachTrailersFor(event.getEntity()); }

    private static void detachTrailersFor(Entity leavingEntity) {
        Level level = leavingEntity.level();
        if (level.isClientSide()) return;
        level.getEntitiesOfClass(AbstractTrailerEntity.class,
                leavingEntity.getBoundingBox().inflate(SEARCH_RADIUS),
                trailer -> { Entity tower = trailer.getTower(); return tower != null && tower.getUUID().equals(leavingEntity.getUUID()); }
        ).forEach(AbstractTrailerEntity::detach);
    }
}
