package frontline.combat.fcp.event;

import com.atsuishio.superbwarfare.api.event.ShootEvent;
import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.effects.FCPMuzzleEffects;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class MuzzleShootEventHandler {
    private MuzzleShootEventHandler() {
    }

    @SubscribeEvent
    public static void onShootPost(ShootEvent.Post event) {
        ShootParameters parameters = event.getParameters();
        Entity ammoSupplier = parameters.ammoSupplier;

        if (!(ammoSupplier instanceof VehicleEntity vehicle) || !FCPMuzzleEffects.isFCPVehicle(vehicle)) {
            return;
        }

        FCPMuzzleEffects.spawnFromShoot(parameters);
    }
}
