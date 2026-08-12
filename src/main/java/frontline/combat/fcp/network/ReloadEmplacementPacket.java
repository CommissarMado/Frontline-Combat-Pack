package frontline.combat.fcp.network;

import frontline.combat.fcp.entity.vehicle.CamoEmplacementEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: "I'm manning an emplacement and pressed the reload key."
 *
 * SBW only drives reloads for hand-held guns (GunEventHandler runs its reload logic behind
 * {@code if (inMainHand)}), so a crew-served weapon needs its own trigger.
 *
 * Carries no data beyond intent: the server re-derives the ridden entity from the sender and
 * requestManualReload() re-checks that they are the gunner, so a spoofed packet can at most
 * reload an emplacement you are already sitting in.
 */
public class ReloadEmplacementPacket {

    public ReloadEmplacementPacket() {
    }

    public static void encode(ReloadEmplacementPacket msg, FriendlyByteBuf buf) {
        // no payload
    }

    public static ReloadEmplacementPacket decode(FriendlyByteBuf buf) {
        return new ReloadEmplacementPacket();
    }

    public static void handle(ReloadEmplacementPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            Entity vehicle = player.getVehicle();
            if (!(vehicle instanceof CamoEmplacementEntity)) {
                vehicle = player.getRootVehicle(); // in case of a proxy seat entity
            }
            if (vehicle instanceof CamoEmplacementEntity emplacement) {
                emplacement.requestManualReload(player);
            }
        });
        context.setPacketHandled(true);
    }
}