package frontline.combat.fcp.client;

import com.mojang.blaze3d.platform.InputConstants;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.screen.FiringSolutionScreen;
import frontline.combat.fcp.entity.vehicle.IndirectFireVehicleBase;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FiringSolutionKeyHandler {

    public static final KeyMapping OPEN_FIRE_CONTROL = new KeyMapping(
            "key.fcp.open_fire_control",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.fcp"
    );

    private FiringSolutionKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        while (OPEN_FIRE_CONTROL.consumeClick()) {
            if (minecraft.screen != null
                    || !(minecraft.player.getVehicle() instanceof IndirectFireVehicleBase vehicle)
                    || vehicle.getSeatIndex(minecraft.player) != vehicle.getTurretControllerIndex()) {
                continue;
            }
            minecraft.setScreen(new FiringSolutionScreen(vehicle, minecraft.player));
        }
    }

    @Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusHandler {
        private ModBusHandler() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_FIRE_CONTROL);
        }
    }
}
