package frontline.combat.fcp.network;

import frontline.combat.fcp.FCP;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * FCP's one network channel. It exists for a single job right now: letting a DRIVING player
 * ask the server to open their vehicle's hold when they press E.
 *
 * Why a packet at all: opening a container must happen server-side, and the only way to do
 * that from a key press — without a channel — was to fake a vehicle right-click. That reused
 * the mount path, and the click that seats you re-ran interact() while you were briefly the
 * driver, so mounting also opened the hold. A dedicated one-way packet removes that entirely:
 * E sends this, nothing else does, and it never touches the mount path.
 *
 * Register once from the FCP main class constructor:
 *     FCPNetwork.register();
 */
public final class FCPNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel FCP_HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(FCP.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private FCPNetwork() {
    }

    public static void register() {
        int id = 0;
        FCP_HANDLER.registerMessage(id++, OpenVehicleHoldPacket.class,
                OpenVehicleHoldPacket::encode, OpenVehicleHoldPacket::decode,
                OpenVehicleHoldPacket::handle);
        FCP_HANDLER.registerMessage(id++, RequestFcpConfigStatePacket.class,
                RequestFcpConfigStatePacket::encode, RequestFcpConfigStatePacket::decode,
                RequestFcpConfigStatePacket::handle);
        FCP_HANDLER.registerMessage(id++, SetMulticrewPacket.class,
                SetMulticrewPacket::encode, SetMulticrewPacket::decode,
                SetMulticrewPacket::handle);
        FCP_HANDLER.registerMessage(id++, FcpConfigStatePacket.class,
                FcpConfigStatePacket::encode, FcpConfigStatePacket::decode,
                FcpConfigStatePacket::handle);
        FCP_HANDLER.registerMessage(id++, ReloadEmplacementPacket.class,
                ReloadEmplacementPacket::encode, ReloadEmplacementPacket::decode,
                ReloadEmplacementPacket::handle);
    }
}