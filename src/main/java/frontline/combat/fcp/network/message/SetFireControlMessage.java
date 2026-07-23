package frontline.combat.fcp.network.message;

import frontline.combat.fcp.entity.vehicle.IndirectFireVehicleBase;
import frontline.combat.fcp.firecontrol.TrajectoryMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetFireControlMessage(
        int entityId,
        boolean clear,
        BlockPos target,
        int radius,
        TrajectoryMode trajectoryMode
) {

    public static SetFireControlMessage apply(
            int entityId,
            BlockPos target,
            int radius,
            TrajectoryMode trajectoryMode
    ) {
        return new SetFireControlMessage(entityId, false, target, radius, trajectoryMode);
    }

    public static SetFireControlMessage clear(int entityId) {
        return new SetFireControlMessage(entityId, true, BlockPos.ZERO, 0, TrajectoryMode.LOW);
    }

    public static void encode(SetFireControlMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeBoolean(message.clear);
        buffer.writeBlockPos(message.target);
        buffer.writeVarInt(message.radius);
        buffer.writeByte(message.trajectoryMode.ordinal());
    }

    public static SetFireControlMessage decode(FriendlyByteBuf buffer) {
        return new SetFireControlMessage(
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBlockPos(),
                buffer.readVarInt(),
                TrajectoryMode.fromId(buffer.readByte())
        );
    }

    public static void handle(
            SetFireControlMessage message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            Entity entity = player.level().getEntity(message.entityId);
            if (!(entity instanceof IndirectFireVehicleBase vehicle)
                    || player.getVehicle() != vehicle
                    || vehicle.getSeatIndex(player) != vehicle.getTurretControllerIndex()) {
                return;
            }

            if (message.clear) {
                vehicle.clearFireControl(player);
            } else {
                vehicle.applyFireControl(
                        message.target,
                        message.radius,
                        message.trajectoryMode,
                        player
                );
            }
        });
        context.setPacketHandled(true);
    }
}
