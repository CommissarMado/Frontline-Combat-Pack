package frontline.combat.fcp.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

/**
 * Server -> client: "you are now remotely operating launcher {@code launcherId}" (or -1: you stopped).
 * The client moves its camera to that launcher's sight and redirects input while it is set.
 */
public class StugnaRemoteStatePacket {

    private final int launcherId;

    public StugnaRemoteStatePacket(int launcherId) {
        this.launcherId = launcherId;
    }

    public static void encode(StugnaRemoteStatePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.launcherId);
    }

    public static StugnaRemoteStatePacket decode(FriendlyByteBuf buf) {
        return new StugnaRemoteStatePacket(buf.readVarInt());
    }

    public static void handle(StugnaRemoteStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // Indirection keeps client classes off dedicated-server classloading.
                frontline.combat.fcp.FCP.runOnClient(() -> () ->
                        frontline.combat.fcp.client.StugnaRemoteClient.setLauncher(msg.launcherId)));
        ctx.get().setPacketHandled(true);
    }
}
