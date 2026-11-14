package frontline.combat.fcp.vehicle;

import frontline.combat.fcp.FCP;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

/**
 * fcp:camo_net (placeholder item): using it on a CamoNetVehicle (Abrams, Leopard 2) cycles the camo net - none, light, heavy
 * where it has one - and sneak-use its colour (jungle, woodland, olive, timber, desert),
 * anywhere on the tank, apart from the Humvee-style attachment system (no attachment boxes). The net itself is drawn by
 * the client (VehicleCamoNet).
 */
@EventBusSubscriber(modid = FCP.MODID)
public final class CamoNetHandler {

    private CamoNetHandler() {
    }

    @SubscribeEvent
    public static void onInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getItemStack().getItem() != ModItems.CAMO_NET.get() || !(event.getTarget() instanceof CamoNetVehicle tank) || !(event.getTarget() instanceof VehicleEntity vehicle)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!event.getLevel().isClientSide()) {
            if (vehicle.isWreck()) { // no net on a wreck
                event.getEntity().displayClientMessage(Component.translatable("tips.fcp.camo_net.wreck"), true);
                return;
            }
            // use: next preset (none, light, heavy); sneak-use: next colour (jungle, woodland, olive, timber, desert)
            String tip = event.getEntity().isShiftKeyDown()
                    ? "tips.fcp.camo_net.color." + tank.cycleCamoNetColor()
                    : "tips.fcp.camo_net." + tank.cycleCamoNet();
            event.getLevel().playSound(null, vehicle.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0F, 1.0F);
            event.getEntity().displayClientMessage(Component.translatable(tip), true);
        }
        event.getEntity().swing(event.getHand());
    }
}
