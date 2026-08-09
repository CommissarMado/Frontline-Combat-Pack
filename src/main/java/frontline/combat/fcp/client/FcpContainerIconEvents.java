package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.init.ModItems;
import frontline.combat.fcp.FCP;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registers FCP's container-icon decorator on SBW's container item. Self-registering. */
@Mod.EventBusSubscriber(modid = FCP.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FcpContainerIconEvents {

    @SubscribeEvent
    public static void onRegisterDecorations(RegisterItemDecorationsEvent event) {
        event.register(ModItems.CONTAINER.get(), new FcpContainerIconDecorator());
    }
}
