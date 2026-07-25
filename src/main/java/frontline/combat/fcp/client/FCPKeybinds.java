package frontline.combat.fcp.client;

import com.mojang.blaze3d.platform.InputConstants;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.screen.FCPConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * FCP keybinds. One so far: open the config screen in-game (default F8, rebindable under
 * its own "Frontline Combat Pack" category in Controls). IN_GAME conflict context plus the
 * open-screen check means it can't fire while typing in chat or another screen.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FCPKeybinds {

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.fcp.open_config",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "key.categories.fcp");

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
        // Poll on the game bus; registered here so the listener exists exactly once and
        // only on the client.
        MinecraftForge.EVENT_BUS.addListener(FCPKeybinds::onClientTick);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_CONFIG.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new FCPConfigScreen(null)); // null parent: closing returns to the game
            }
        }
    }
}