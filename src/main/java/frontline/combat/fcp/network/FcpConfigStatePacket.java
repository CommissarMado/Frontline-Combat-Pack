package frontline.combat.fcp.network;

import frontline.combat.fcp.FcpWorldSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * S2C: the server's multicrew state, whether THIS player may change settings, and the world
 * settings (inventory lock, artillery gravity multipliers - clients need those for trajectories).
 */
public class FcpConfigStatePacket {

    private final boolean multicrew;
    private final boolean canEdit;
    private final FcpWorldSettings.Snapshot world;

    public FcpConfigStatePacket(boolean multicrew, boolean canEdit, FcpWorldSettings.Snapshot world) {
        this.multicrew = multicrew;
        this.canEdit = canEdit;
        this.world = world;
    }

    public static void encode(FcpConfigStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.multicrew);
        buf.writeBoolean(msg.canEdit);
        buf.writeBoolean(msg.world.lockInventories());
        buf.writeBoolean(msg.world.fillNewVehicles());
        buf.writeBoolean(msg.world.armorModel());
        buf.writeFloat(msg.world.artilleryWorld());
        buf.writeMap(msg.world.artillery(), FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeFloat);
    }

    public static FcpConfigStatePacket decode(FriendlyByteBuf buf) {
        boolean multicrew = buf.readBoolean(), canEdit = buf.readBoolean(), lock = buf.readBoolean(), fill = buf.readBoolean(), armor = buf.readBoolean();
        float world = buf.readFloat();
        Map<String, Float> artillery = buf.readMap(LinkedHashMap::new, FriendlyByteBuf::readUtf, FriendlyByteBuf::readFloat);
        return new FcpConfigStatePacket(multicrew, canEdit, new FcpWorldSettings.Snapshot(lock, fill, armor, world, artillery));
    }

    public static void handle(FcpConfigStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // Indirection keeps the Screen class off dedicated-server classloading.
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    FcpWorldSettings.acceptFromServer(msg.world);
                    frontline.combat.fcp.client.screen.FCPConfigScreen.acceptServerState(msg.multicrew, msg.canEdit);
                }));
        ctx.get().setPacketHandled(true);
    }
}
