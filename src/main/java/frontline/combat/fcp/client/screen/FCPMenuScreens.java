package frontline.combat.fcp.client.screen;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.VehicleInventoryKeyHandler;
import frontline.combat.fcp.init.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Binds FCP menu types to their screens. Self-registering — no wiring needed. */
@Mod.EventBusSubscriber(modid = FCP.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FCPMenuScreens {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.VEHICLE_INVENTORY.get(), VehicleInventoryScreen::new);
            // Register the E-while-driving handler here, on the client-setup path that's
            // already proven to run (this class registers the screen you can see working).
            VehicleInventoryKeyHandler.register();
        });
    }
}