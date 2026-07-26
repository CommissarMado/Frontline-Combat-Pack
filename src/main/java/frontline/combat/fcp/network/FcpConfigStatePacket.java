package frontline.combat.fcp.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S2C: the server's multicrew state plus whether THIS player may change it. */
public class FcpConfigStatePacket {

    private final boolean multicrew;
    private final boolean canEdit;

    public FcpConfigStatePacket(boolean multicrew, boolean canEdit) {
        this.multicrew = multicrew;
        this.canEdit = canEdit;
    }

    public static void encode(FcpConfigStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.multicrew);
        buf.writeBoolean(msg.canEdit);
    }

    public static FcpConfigStatePacket decode(FriendlyByteBuf buf) {
        return new FcpConfigStatePacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(FcpConfigStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // Indirection keeps the Screen class off dedicated-server classloading.
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        frontline.combat.fcp.client.screen.FCPConfigScreen.acceptServerState(
                                msg.multicrew, msg.canEdit)));
        ctx.get().setPacketHandled(true);
    }
}