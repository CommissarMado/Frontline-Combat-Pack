package frontline.combat.fcp.network;

import frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Client -> server: the HIMARS gunner's entered coordinates (SBW keeps them client-side until a shot), sent whenever
 * they change, so coordinate mode can keep the launcher locked on the target before the first missile. The server
 * accepts it only from the sender's own HIMARS turret-controller seat.
 */
public class HimarsTargetPacket {

    private final @Nullable BlockPos pos;

    public HimarsTargetPacket(@Nullable BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(HimarsTargetPacket msg, FriendlyByteBuf buf) {
        buf.writeNullable(msg.pos, FriendlyByteBuf::writeBlockPos);
    }

    public static HimarsTargetPacket decode(FriendlyByteBuf buf) {
        return new HimarsTargetPacket(buf.readNullable(FriendlyByteBuf::readBlockPos));
    }

    public static void handle(HimarsTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getVehicle() instanceof HimarsEntity himars
                    && himars.getSeatIndex(player) == himars.getTurretControllerIndex()) {
                himars.setCoordTarget(msg.pos);
            }
        });
        context.setPacketHandled(true);
    }
}
