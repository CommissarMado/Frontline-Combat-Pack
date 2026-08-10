package frontline.combat.fcp.network;

import frontline.combat.fcp.entity.vehicle.VehicleInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: "I'm driving a vehicle with a hold and pressed E — open it."
 *
 * Carries no data beyond intent. The server doesn't trust the client about WHICH vehicle or
 * WHETHER it may open it — it re-derives the ridden vehicle from the sender and re-checks
 * that they're aboard, so a spoofed packet can at most open the hold of a vehicle you're
 * actually riding, which you could do anyway.
 */
public class OpenVehicleHoldPacket {

    public OpenVehicleHoldPacket() {
    }

    public static void encode(OpenVehicleHoldPacket msg, FriendlyByteBuf buf) {
        // no payload
    }

    public static OpenVehicleHoldPacket decode(FriendlyByteBuf buf) {
        return new OpenVehicleHoldPacket();
    }

    public static void handle(OpenVehicleHoldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            Entity vehicle = player.getVehicle();
            if (!(vehicle instanceof VehicleInventory)) {
                vehicle = player.getRootVehicle(); // in case of a proxy seat entity
            }
            if (vehicle instanceof VehicleInventory camo) {
                // openHoldForRider re-checks riding + opensVehicleScreen server-side, so a
                // spoofed packet can only open a hold you're actually aboard.
                camo.openHoldForRider(player);
            }
        });
        context.setPacketHandled(true);
    }
}