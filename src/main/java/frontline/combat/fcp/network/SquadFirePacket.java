package frontline.combat.fcp.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: one shot from a vehicle weapon with Squad fire sounds (see SquadWeaponFire). */
public class SquadFirePacket {

    private final int vehicleId;
    private final String weapon;
    private final int shooterId;

    public SquadFirePacket(int vehicleId, String weapon, int shooterId) {
        this.vehicleId = vehicleId;
        this.weapon = weapon;
        this.shooterId = shooterId;
    }

    public static void encode(SquadFirePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.vehicleId);
        buf.writeUtf(msg.weapon, 64);
        buf.writeVarInt(msg.shooterId);
    }

    public static SquadFirePacket decode(FriendlyByteBuf buf) {
        return new SquadFirePacket(buf.readVarInt(), buf.readUtf(64), buf.readVarInt());
    }

    public static void handle(SquadFirePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // Indirection keeps client classes off dedicated-server classloading.
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        frontline.combat.fcp.client.sound.SquadWeaponSounds.onShot(msg.vehicleId, msg.weapon, msg.shooterId)));
        ctx.get().setPacketHandled(true);
    }
}
