package frontline.combat.fcp.entity.vehicle;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModEntities;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Hands NeoForge the item handlers of FCP's vehicles (see {@link FcpItemHandlerSource}).
 * <p>
 * Superb Warfare registers an item-handler provider for every entity type too. Providers are asked
 * in registration order and the first non-null answer wins, so this listener runs at HIGHEST
 * priority to get in front of SBW's.
 */
@EventBusSubscriber(modid = FCP.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class FcpCapabilities {

    private FcpCapabilities() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void register(RegisterCapabilitiesEvent event) {
        ModEntities.ENTITY_TYPES.getEntries().forEach(type -> event.registerEntity(
                Capabilities.ItemHandler.ENTITY, type.get(),
                (entity, context) -> entity instanceof FcpItemHandlerSource source ? source.fcp$itemHandler() : null));
    }
}
