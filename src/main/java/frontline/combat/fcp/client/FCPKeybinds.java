package frontline.combat.fcp.client;

import com.mojang.blaze3d.platform.InputConstants;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.screen.FCPConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * FCP keybinds. One so far: open the config screen in-game (default F8, rebindable under
 * its own "Frontline Combat Pack" category in Controls). IN_GAME conflict context plus the
 * open-screen check means it can't fire while typing in chat or another screen.
 */
@EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class FCPKeybinds {

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.fcp.open_config",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "key.categories.fcp");

    /** Hold as the driver to start/stop the engine (EngineKeyHandler). */
    public static final KeyMapping ENGINE_TOGGLE = new KeyMapping(
            "key.fcp.engine_toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.fcp");

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
        event.register(ENGINE_TOGGLE);
        // Poll on the game bus; registered here so the listener exists exactly once and
        // only on the client.
        NeoForge.EVENT_BUS.addListener(FCPKeybinds::onClientTick);
    }

    private static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_CONFIG.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new FCPConfigScreen(null)); // null parent: closing returns to the game
            }
        }
    }
}