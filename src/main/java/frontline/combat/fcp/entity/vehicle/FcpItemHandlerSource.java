package frontline.combat.fcp.entity.vehicle;

import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * An entity that supplies its own item handler in place of Superb Warfare's. SBW resolves a
 * vehicle's ammo through the entity item-handler capability; {@link FcpCapabilities} registers this
 * ahead of SBW's provider, so returning a handler here redirects that whole pipeline. Returning
 * null falls through to SBW's own container.
 */
public interface FcpItemHandlerSource {
    @Nullable IItemHandler fcp$itemHandler();
}
