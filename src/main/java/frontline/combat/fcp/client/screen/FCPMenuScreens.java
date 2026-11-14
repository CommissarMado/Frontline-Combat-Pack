package frontline.combat.fcp.client.screen;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.VehicleInventoryKeyHandler;
import frontline.combat.fcp.init.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Binds FCP menu types to their screens. Self-registering — no wiring needed. */
@EventBusSubscriber(modid = FCP.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FCPMenuScreens {

    @SubscribeEvent
    public static void onRegisterScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(ModMenus.VEHICLE_INVENTORY.get(), VehicleInventoryScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register the E-while-driving handler here, on the client-setup path that's
            // already proven to run (this class registers the screen you can see working).
            VehicleInventoryKeyHandler.register();
        });
    }
}