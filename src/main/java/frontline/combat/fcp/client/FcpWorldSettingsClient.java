package frontline.combat.fcp.client;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.FcpWorldSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Drops the last server's world settings on leaving it, so they can't leak into the next world. */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class FcpWorldSettingsClient {

    private FcpWorldSettingsClient() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FcpWorldSettings.acceptFromServer(FcpWorldSettings.Snapshot.DEFAULT);
    }
}
