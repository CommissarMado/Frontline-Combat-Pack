package frontline.combat.fcp.network;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.GunnerSmoke;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server, sent every tick while the gunner holds SBW's release-decoy key.
 *
 * Carries no data: the server re-derives the vehicle from the sender and re-checks that it is a
 * gunner-smoke vehicle and that they sit in its turret-controller seat.
 */
public class GunnerSmokePacket {

    public GunnerSmokePacket() {
    }

    public static void encode(GunnerSmokePacket msg, FriendlyByteBuf buf) {
        // no payload
    }

    public static GunnerSmokePacket decode(FriendlyByteBuf buf) {
        return new GunnerSmokePacket();
    }

    public static void handle(GunnerSmokePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getVehicle() instanceof VehicleEntity vehicle
                    && GunnerSmoke.isSmokeOperator(vehicle, player)) {
                GunnerSmoke.hold(vehicle);
            }
        });
        context.setPacketHandled(true);
    }
}
