package frontline.combat.fcp.client;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.FcpWorldSettings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

/** Drops the last server's world settings on leaving it, so they can't leak into the next world. */
@EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class FcpWorldSettingsClient {

    private FcpWorldSettingsClient() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FcpWorldSettings.acceptFromServer(FcpWorldSettings.Snapshot.DEFAULT);
    }
}
