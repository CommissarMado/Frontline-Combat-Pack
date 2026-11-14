package frontline.combat.fcp.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * The slice of Forge's old NetworkEvent that FCP's packet handlers use, on top of NeoForge's
 * payload context, so the handlers themselves read the same as before.
 */
public final class NetworkEvent {

    private NetworkEvent() {
    }

    public static final class Context {
        private final IPayloadContext context;

        Context(IPayloadContext context) {
            this.context = context;
        }

        public CompletableFuture<Void> enqueueWork(Runnable work) {
            return context.enqueueWork(work);
        }

        /** The player who sent the packet, or null when it arrived on the client. */
        public @Nullable ServerPlayer getSender() {
            return context.player() instanceof ServerPlayer player ? player : null;
        }

        /** Payloads are always consumed by their handler; kept so the handlers need no change. */
        public void setPacketHandled(boolean handled) {
        }
    }
}
