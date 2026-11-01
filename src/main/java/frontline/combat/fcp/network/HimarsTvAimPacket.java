package frontline.combat.fcp.network;

import frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: the heading (yaw / pitch) a TV-mode GMLRS pilot is steering for, sent when it changes. The missile
 * ignores it from anyone but its own pilot.
 */
public class HimarsTvAimPacket {

    private final int missileId;
    private final float yaw, pitch;

    public HimarsTvAimPacket(int missileId, float yaw, float pitch) {
        this.missileId = missileId;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static void encode(HimarsTvAimPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.missileId);
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
    }

    public static HimarsTvAimPacket decode(FriendlyByteBuf buf) {
        return new HimarsTvAimPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(HimarsTvAimPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && Float.isFinite(msg.yaw) && Float.isFinite(msg.pitch)
                    && player.level().getEntity(msg.missileId) instanceof HimarsMissileEntity missile) {
                missile.setPilotAim(player, msg.yaw, msg.pitch);
            }
        });
        context.setPacketHandled(true);
    }
}
