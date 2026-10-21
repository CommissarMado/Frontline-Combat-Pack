package frontline.combat.fcp.network;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.engine.EngineControl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server engine key input from the driver.
 *
 * HOLD is sent every tick while the key is held for a start (see EngineControl.holdStart);
 * STOP once on a press while the engine runs. The server re-derives the vehicle from the sender
 * and checks they are its driver (first passenger).
 */
public class EngineControlPacket {

    public static final byte HOLD = 0;
    public static final byte STOP = 1;

    private final byte action;

    public EngineControlPacket(byte action) {
        this.action = action;
    }

    public static void encode(EngineControlPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.action);
    }

    public static EngineControlPacket decode(FriendlyByteBuf buf) {
        return new EngineControlPacket(buf.readByte());
    }

    public static void handle(EngineControlPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getVehicle() instanceof VehicleEntity vehicle
                    && vehicle.getFirstPassenger() == player) {
                if (msg.action == HOLD) EngineControl.holdStart(vehicle);
                else if (msg.action == STOP) EngineControl.stop(vehicle);
            }
        });
        context.setPacketHandled(true);
    }
}
