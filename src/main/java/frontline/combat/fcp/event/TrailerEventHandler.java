package frontline.combat.fcp.event;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * TrailerEventHandler — detaches trailers whose driver dies or leaves the level
 * (killed, unloaded, dimension change, etc.) so they don't snap to a stale point.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public class TrailerEventHandler {

    private static final double SEARCH_RADIUS = 32.0;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        detachTrailersFor(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        detachTrailersFor(event.getEntity());
    }

    private static void detachTrailersFor(Entity leaving) {
        Level level = leaving.level();
        if (level.isClientSide()) return;

        level.getEntitiesOfClass(
                AbstractTrailerEntity.class,
                leaving.getBoundingBox().inflate(SEARCH_RADIUS),
                trailer -> {
                    Entity driver = trailer.getDriver();
                    return driver != null && driver.getUUID().equals(leaving.getUUID());
                }
        ).forEach(AbstractTrailerEntity::detach);
    }
}