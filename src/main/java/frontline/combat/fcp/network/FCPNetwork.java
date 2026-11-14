package frontline.combat.fcp.network;

import frontline.combat.fcp.FCP;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    /**
     * Every FCP packet keeps its plain encode/decode/handle statics; on the wire each travels inside
     * an Envelope, which is what NeoForge's payload system registers and routes.
     */
    private record Envelope<T>(CustomPacketPayload.Type<Envelope<T>> type, T message) implements CustomPacketPayload {
    }

    private static final Map<Class<?>, CustomPacketPayload.Type<?>> TYPES = new HashMap<>();

    /** Where a server-to-client packet goes. See {@link #player} and {@link #tracking}. */
    public interface Target extends Consumer<CustomPacketPayload> {
    }

    public static Target player(ServerPlayer player) {
        return payload -> PacketDistributor.sendToPlayer(player, payload);
    }

    /** Every player tracking the entity (not the entity itself). */
    public static Target tracking(Entity entity) {
        return payload -> PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static final class Channel {
        private Channel() {
        }

        public void sendToServer(Object message) {
            PacketDistributor.sendToServer(wrap(message));
        }

        public void send(Target target, Object message) {
            target.accept(wrap(message));
        }
    }

    public static final Channel FCP_HANDLER = new Channel();

    private FCPNetwork() {
    }

    @SuppressWarnings("unchecked")
    private static <T> CustomPacketPayload wrap(T message) {
        CustomPacketPayload.Type<Envelope<T>> type = (CustomPacketPayload.Type<Envelope<T>>) TYPES.get(message.getClass());
        if (type == null) throw new IllegalArgumentException("Unregistered FCP packet " + message.getClass().getName());
        return new Envelope<>(type, message);
    }

    private static <T> void message(PayloadRegistrar registrar, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> encode,
                                    Function<FriendlyByteBuf, T> decode, BiConsumer<T, Supplier<NetworkEvent.Context>> handle) {
        // e.g. OpenVehicleHoldPacket -> fcp:open_vehicle_hold
        String path = clazz.getSimpleName().replaceAll("Packet$", "").replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        CustomPacketPayload.Type<Envelope<T>> type = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FCP.MODID, path));
        TYPES.put(clazz, type);
        StreamCodec<RegistryFriendlyByteBuf, Envelope<T>> codec = StreamCodec.of(
                (buf, envelope) -> encode.accept(envelope.message(), buf),
                buf -> new Envelope<>(type, decode.apply(buf)));
        registrar.playBidirectional(type, codec, (envelope, context) -> {
            NetworkEvent.Context wrapped = new NetworkEvent.Context(context);
            handle.accept(envelope.message(), () -> wrapped);
        });
    }

    /** Mod-bus listener; hooked up from the FCP main class. */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        message(registrar, OpenVehicleHoldPacket.class, OpenVehicleHoldPacket::encode, OpenVehicleHoldPacket::decode, OpenVehicleHoldPacket::handle);
        message(registrar, RequestFcpConfigStatePacket.class, RequestFcpConfigStatePacket::encode, RequestFcpConfigStatePacket::decode, RequestFcpConfigStatePacket::handle);
        message(registrar, SetMulticrewPacket.class, SetMulticrewPacket::encode, SetMulticrewPacket::decode, SetMulticrewPacket::handle);
        message(registrar, FcpConfigStatePacket.class, FcpConfigStatePacket::encode, FcpConfigStatePacket::decode, FcpConfigStatePacket::handle);
        message(registrar, SetWorldSettingPacket.class, SetWorldSettingPacket::encode, SetWorldSettingPacket::decode, SetWorldSettingPacket::handle);
        message(registrar, ReloadEmplacementPacket.class, ReloadEmplacementPacket::encode, ReloadEmplacementPacket::decode, ReloadEmplacementPacket::handle);
        message(registrar, StugnaRemoteStatePacket.class, StugnaRemoteStatePacket::encode, StugnaRemoteStatePacket::decode, StugnaRemoteStatePacket::handle);
        message(registrar, StugnaRemoteActionPacket.class, StugnaRemoteActionPacket::encode, StugnaRemoteActionPacket::decode, StugnaRemoteActionPacket::handle);
        message(registrar, StugnaAimPacket.class, StugnaAimPacket::encode, StugnaAimPacket::decode, StugnaAimPacket::handle);
        message(registrar, GunnerSmokePacket.class, GunnerSmokePacket::encode, GunnerSmokePacket::decode, GunnerSmokePacket::handle);
        message(registrar, EngineControlPacket.class, EngineControlPacket::encode, EngineControlPacket::decode, EngineControlPacket::handle);
        message(registrar, SquadFirePacket.class, SquadFirePacket::encode, SquadFirePacket::decode, SquadFirePacket::handle);
        message(registrar, HimarsTargetPacket.class, HimarsTargetPacket::encode, HimarsTargetPacket::decode, HimarsTargetPacket::handle);
        message(registrar, HimarsTvAimPacket.class, HimarsTvAimPacket::encode, HimarsTvAimPacket::decode, HimarsTvAimPacket::handle);
    }
}
