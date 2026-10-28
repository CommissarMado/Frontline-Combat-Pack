package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import frontline.combat.fcp.FCP;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
import frontline.combat.fcp.entity.vehicle.T72.T72Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Narrower default view for a main gunner looking through the sight. SBW only zooms the vehicle
 * camera while the zoom key is held (the gun's DefaultZoom); this is the view without it, so the
 * Abrams and T-72/T-90 gunners' sights start magnified like the real wide-field optics.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class GunnerSightFov {

    private static final double TANK_GUNNER_ZOOM = 2.0;

    private GunnerSightFov() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || ClientEventHandler.zoomVehicle || !mc.options.getCameraType().isFirstPerson()) return;
        if (!(player.getVehicle() instanceof VehicleEntity tank) || !(tank instanceof M1a1Entity || tank instanceof T72Entity || tank instanceof frontline.combat.fcp.entity.vehicle.T90M.T90MEntity)) return;
        if (tank.getSeatIndex(player) != tank.computed().getTurretControllerIndex()) return;
        event.setFOV(event.getFOV() / TANK_GUNNER_ZOOM);
    }
}
