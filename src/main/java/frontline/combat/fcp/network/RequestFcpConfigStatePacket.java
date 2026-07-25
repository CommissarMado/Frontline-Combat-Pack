package frontline.combat.fcp.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** C2S: the config screen asking for the server's current state. Reply: FcpConfigStatePacket. */
public class RequestFcpConfigStatePacket {

    public static void encode(RequestFcpConfigStatePacket msg, FriendlyByteBuf buf) {
    }

    public static RequestFcpConfigStatePacket decode(FriendlyByteBuf buf) {
        return new RequestFcpConfigStatePacket();
    }

    public static void handle(RequestFcpConfigStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) SetMulticrewPacket.sendStateTo(player);
        });
        ctx.get().setPacketHandled(true);
    }
}