package frontline.combat.fcp.network;

import frontline.combat.fcp.FcpWorldSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S: change one per-world setting (FcpWorldSettings). Same permission as multicrew: operators,
 * or the singleplayer/LAN host. Everyone online gets the new state.
 */
public class SetWorldSettingPacket {

    /** Gravity multipliers outside this range make artillery useless or fire off the map. */
    public static final float MIN_MULTIPLIER = 0.05f, MAX_MULTIPLIER = 20f;

    public enum Kind { LOCK_INVENTORIES, FILL_NEW_VEHICLES, ARMOR_MODEL, ARTILLERY_WORLD, ARTILLERY_VEHICLE }

    private final Kind kind;
    private final String vehicle;
    private final float value;

    public SetWorldSettingPacket(Kind kind, String vehicle, float value) {
        this.kind = kind;
        this.vehicle = vehicle;
        this.value = value;
    }

    public static void encode(SetWorldSettingPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.kind);
        buf.writeUtf(msg.vehicle);
        buf.writeFloat(msg.value);
    }

    public static SetWorldSettingPacket decode(FriendlyByteBuf buf) {
        return new SetWorldSettingPacket(buf.readEnum(Kind.class), buf.readUtf(), buf.readFloat());
    }

    public static void handle(SetWorldSettingPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!SetMulticrewPacket.mayEdit(player)) {
                player.displayClientMessage(Component.translatable("gui.fcp.config.denied"), true);
                SetMulticrewPacket.sendStateTo(player);
                return;
            }
            FcpWorldSettings settings = FcpWorldSettings.get(player.server);
            float multiplier = Float.isFinite(msg.value) ? Mth.clamp(msg.value, MIN_MULTIPLIER, MAX_MULTIPLIER) : 1f;
            switch (msg.kind) {
                case LOCK_INVENTORIES -> settings.setLockInventories(msg.value != 0);
                case FILL_NEW_VEHICLES -> settings.setFillNewVehicles(msg.value != 0);
                case ARMOR_MODEL -> settings.setArmorModel(msg.value != 0);
                case ARTILLERY_WORLD -> settings.setArtilleryWorld(multiplier);
                case ARTILLERY_VEHICLE -> settings.setArtillery(msg.vehicle, multiplier);
            }
            player.server.getPlayerList().getPlayers().forEach(SetMulticrewPacket::sendStateTo);
        });
        ctx.get().setPacketHandled(true);
    }
}
