package frontline.combat.fcp.network;

import frontline.combat.fcp.entity.vehicle.Stugna.StugnaRemote;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: the remote Stugna operator pressed fire or asked to disconnect.
 *
 * SBW's own vehicle fire packet only accepts a player riding the vehicle, and the operator never
 * rides the launcher. The server re-checks the sender's session, so a spoofed packet can at most
 * fire a launcher the sender is already operating.
 */
public class StugnaRemoteActionPacket {

    public static final byte FIRE = 0;
    public static final byte DISCONNECT = 1;

    private final byte action;

    public StugnaRemoteActionPacket(byte action) {
        this.action = action;
    }

    public static void encode(StugnaRemoteActionPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.action);
    }

    public static StugnaRemoteActionPacket decode(FriendlyByteBuf buf) {
        return new StugnaRemoteActionPacket(buf.readByte());
    }

    public static void handle(StugnaRemoteActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (msg.action == FIRE) {
                StugnaRemote.fire(player);
            } else if (msg.action == DISCONNECT) {
                StugnaRemote.stop(player, "Stugna-P disconnected");
            }
        });
        context.setPacketHandled(true);
    }
}
