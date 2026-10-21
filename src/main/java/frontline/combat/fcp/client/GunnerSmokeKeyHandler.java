package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModKeyMappings;
import frontline.combat.fcp.entity.vehicle.GunnerSmoke;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.GunnerSmokePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Lets the gunner fire smoke on the vehicles listed in GunnerSmoke, using SBW's own release-decoy key.
 *
 * SBW only reads that key as part of the driver's movement input, so the gunner's press never reached
 * the vehicle. While the gunner holds it, this sends GunnerSmokePacket every tick; the server feeds
 * that into the vehicle's decoy input (and ignores the driver's) - see GunnerSmoke.
 */
@Mod.EventBusSubscriber(modid = "fcp", value = Dist.CLIENT)
public final class GunnerSmokeKeyHandler {

    private GunnerSmokeKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null) return;

        if (ModKeyMappings.RELEASE_DECOY.isDown()
                && player.getVehicle() instanceof VehicleEntity vehicle
                && GunnerSmoke.isSmokeOperator(vehicle, player)) {
            FCPNetwork.FCP_HANDLER.sendToServer(new GunnerSmokePacket());
        }
    }
}
