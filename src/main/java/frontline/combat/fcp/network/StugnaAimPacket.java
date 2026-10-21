package frontline.combat.fcp.network;

import frontline.combat.fcp.entity.vehicle.Stugna.StugnaRemote;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: the remote Stugna operator's sight aim (world yaw / pitch).
 *
 * The operator's own body stays facing the control panel, so the sight direction can't be taken from
 * their rotation; the client accumulates mouse movement into a separate aim and sends it here.
 */
public class StugnaAimPacket {

    private final float yaw;
    private final float pitch;

    public StugnaAimPacket(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static void encode(StugnaAimPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
    }

    public static StugnaAimPacket decode(FriendlyByteBuf buf) {
        return new StugnaAimPacket(buf.readFloat(), buf.readFloat());
    }

    public static void handle(StugnaAimPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && Float.isFinite(msg.yaw) && Float.isFinite(msg.pitch)) {
                StugnaRemote.setAim(player, msg.yaw, msg.pitch);
            }
        });
        context.setPacketHandled(true);
    }
}
