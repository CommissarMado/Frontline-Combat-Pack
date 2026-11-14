package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.init.ModItems;
import frontline.combat.fcp.FCP;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

/** Registers FCP's container-icon decorator on SBW's container item. Self-registering. */
@EventBusSubscriber(modid = FCP.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FcpContainerIconEvents {

    @SubscribeEvent
    public static void onRegisterDecorations(RegisterItemDecorationsEvent event) {
        event.register(ModItems.CONTAINER.get(), new FcpContainerIconDecorator());
    }
}
