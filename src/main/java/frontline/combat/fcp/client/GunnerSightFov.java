package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
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
 * Abrams gunner's sight starts magnified like the real wide-field optic.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class GunnerSightFov {

    private static final double ABRAMS_GUNNER_ZOOM = 2.0;

    private GunnerSightFov() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || ClientEventHandler.zoomVehicle || !mc.options.getCameraType().isFirstPerson()) return;
        if (!(player.getVehicle() instanceof M1a1Entity tank)) return;
        if (tank.getSeatIndex(player) != tank.computed().getTurretControllerIndex()) return;
        event.setFOV(event.getFOV() / ABRAMS_GUNNER_ZOOM);
    }
}
