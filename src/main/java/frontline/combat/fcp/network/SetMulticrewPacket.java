package frontline.combat.fcp.network;

import frontline.combat.fcp.FCPConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * C2S: toggle multicrew for the WHOLE server.
 *
 * Permission: operator level 2, OR being the singleplayer/LAN host — so singleplayer needs
 * nothing (even with cheats off, where hasPermissions(2) is false), while on a server only
 * ops can touch it. On success the config is saved and datapacks hot-reload (same discovery
 * /reload uses), so the fcp_multicrew pack appears/disappears immediately. Every online
 * player gets the new state so any open config screens stay truthful.
 */
public class SetMulticrewPacket {

    private final boolean multicrew;

    public SetMulticrewPacket(boolean multicrew) {
        this.multicrew = multicrew;
    }

    public static void encode(SetMulticrewPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.multicrew);
    }

    public static SetMulticrewPacket decode(FriendlyByteBuf buf) {
        return new SetMulticrewPacket(buf.readBoolean());
    }

    public static void handle(SetMulticrewPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!mayEdit(player)) {
                player.displayClientMessage(Component.translatable("gui.fcp.config.denied"), true);
                sendStateTo(player); // keep their screen honest
                return;
            }

            FCPConfig.setMulticrew(msg.multicrew);
            reloadDataPacks(player.server);

            player.server.getPlayerList().getPlayers().forEach(SetMulticrewPacket::sendStateTo);
            player.displayClientMessage(Component.translatable(
                    msg.multicrew ? "gui.fcp.config.multicrew_on" : "gui.fcp.config.multicrew_off"), true);
        });
        ctx.get().setPacketHandled(true);
    }

    /** Ops on servers; the host in singleplayer/LAN regardless of the cheats setting. */
    public static boolean mayEdit(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null && server.isSingleplayerOwner(player.getGameProfile())) return true;
        return player.hasPermissions(2);
    }

    static void sendStateTo(ServerPlayer player) {
        FCPNetwork.FCP_HANDLER.send(PacketDistributor.PLAYER.with(() -> player),
                new FcpConfigStatePacket(FCPConfig.multicrewEnabled(), mayEdit(player)));
    }

    /**
     * The same pack rediscovery /reload performs: refresh sources (our config-gated pack
     * finder re-runs here), auto-select newly available packs unless explicitly disabled,
     * and reload server resources with the result.
     */
    private static void reloadDataPacks(MinecraftServer server) {
        PackRepository repo = server.getPackRepository();
        List<String> selected = new ArrayList<>(repo.getSelectedIds());
        repo.reload();
        Collection<String> disabled = server.getWorldData().getDataConfiguration().dataPacks().getDisabled();
        for (String id : repo.getAvailableIds()) {
            if (!disabled.contains(id) && !selected.contains(id)) selected.add(id);
        }
        server.reloadResources(selected).exceptionally(err -> {
            org.slf4j.LoggerFactory.getLogger("fcp").error("Datapack reload failed", err);
            return null;
        });
    }
}