package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.platform.InputConstants;
import frontline.combat.fcp.entity.vehicle.engine.EngineControl;
import frontline.combat.fcp.network.EngineControlPacket;
import frontline.combat.fcp.network.FCPNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The driver's engine key (default H).
 *
 * Engine off: hold to start - HOLD goes to the server every tick while the key is down, and
 * letting go before the engine catches aborts the start. Engine running: hold for half a second to shut it down.
 * A press that stopped the engine has to be released before the next start.
 *
 * The key is read straight from GLFW as well as through the mapping: H is also SBW's edit-mode
 * key, and when two mappings share a key only one of them is guaranteed to see the press.
 *
 * Also times the start for the HUD bar, from when this client saw the engine enter STARTING.
 */
@Mod.EventBusSubscriber(modid = "fcp", value = Dist.CLIENT)
public final class EngineKeyHandler {

    /** Ticks the key must be held to shut the engine down. */
    private static final int STOP_HOLD_TICKS = 10;

    private static boolean wasDown;
    /** Ticks the current press has been held while running, toward a shutdown; -1 when not counting. */
    private static int stopHeld = -1;
    /** True while the current press is a start hold (began with the engine off). */
    private static boolean starting;

    private static VehicleEntity hudVehicle;
    private static byte hudState = -1;
    private static int startObservedTick;

    private EngineKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        VehicleEntity vehicle = player != null && player.getVehicle() instanceof VehicleEntity v
                && v.getFirstPassenger() == player && EngineControl.appliesTo(v) ? v : null;

        trackStart(vehicle);

        boolean down = vehicle != null && mc.screen == null && isKeyDown(mc);
        if (down && !wasDown) {
            byte state = EngineControl.state(vehicle);
            if (state == EngineControl.RUNNING) {
                stopHeld = 0;
                starting = false;
            } else {
                starting = true;
            }
        }
        if (!down) {
            starting = false;
            stopHeld = -1;
        }
        if (stopHeld >= 0 && ++stopHeld >= STOP_HOLD_TICKS) {
            FCPNetwork.FCP_HANDLER.sendToServer(new EngineControlPacket(EngineControlPacket.STOP));
            stopHeld = -1;
        }
        // Keep the start alive until it catches; after that the hold no longer matters.
        if (starting && EngineControl.state(vehicle) != EngineControl.RUNNING) {
            FCPNetwork.FCP_HANDLER.sendToServer(new EngineControlPacket(EngineControlPacket.HOLD));
        } else {
            starting = false;
        }
        wasDown = down;
    }

    private static void trackStart(VehicleEntity vehicle) {
        byte state = vehicle == null ? -1 : EngineControl.state(vehicle);
        if (vehicle != hudVehicle || state != hudState) {
            if (state == EngineControl.STARTING) startObservedTick = vehicle.tickCount;
            hudVehicle = vehicle;
            hudState = state;
        }
    }

    /** 0..1 progress of the engine start, for the HUD bar. */
    public static float startProgress(VehicleEntity vehicle, float partialTick) {
        if (vehicle != hudVehicle || hudState != EngineControl.STARTING) return 0f;
        float elapsed = vehicle.tickCount - startObservedTick + partialTick;
        return Mth.clamp(elapsed / EngineControl.startTicks(vehicle), 0f, 1f);
    }

    private static boolean isKeyDown(Minecraft mc) {
        if (FCPKeybinds.ENGINE_TOGGLE.isDown()) return true;
        InputConstants.Key key = FCPKeybinds.ENGINE_TOGGLE.getKey();
        return key.getType() == InputConstants.Type.KEYSYM && key.getValue() != InputConstants.UNKNOWN.getValue()
                && InputConstants.isKeyDown(mc.getWindow().getWindow(), key.getValue());
    }
}
