package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.HimarsTargetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

/**
 * Sends the HIMARS gunner's entered coordinates (SBW's ClientEventHandler.missileLockingPos) to the server whenever
 * they change, and once on taking the gunner seat - see HimarsTargetPacket.
 */
@Mod.EventBusSubscriber(modid = "fcp", value = Dist.CLIENT)
public final class HimarsTargetSync {

    private static HimarsEntity vehicle;
    private static BlockPos sent;

    private HimarsTargetSync() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.getVehicle() instanceof HimarsEntity himars)
                || himars.getSeatIndex(player) != himars.getTurretControllerIndex()) {
            vehicle = null;
            return;
        }
        BlockPos pos = ClientEventHandler.missileLockingPos;
        if (himars == vehicle && Objects.equals(pos, sent)) return;
        vehicle = himars;
        sent = pos;
        FCPNetwork.FCP_HANDLER.sendToServer(new HimarsTargetPacket(pos));
    }
}
