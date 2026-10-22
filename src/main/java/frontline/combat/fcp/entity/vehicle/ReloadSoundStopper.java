package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.SoundInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Stops a vehicle weapon's reload sound when its ammo type is switched mid-reload.
 * <p>
 * SBW starts the reload sound once when a reload begins (1P to the crew, 3P in the world) and
 * never stops it. Switching ammo (GunData.changeAmmoConsumer) restarts the reload, so the old sound
 * kept playing over the new one. Run server-side after every vehicle tick (see
 * VehicleReloadSoundStopMixin): a changed ammo type on a weapon that was reloading sends a stop
 * for that weapon's reload sounds to the players in earshot, a tick before the new reload's own
 * sound starts.
 */
public final class ReloadSoundStopper {

    /** Further than any reload sound carries. */
    private static final double RANGE = 96;

    /** Per vehicle, per weapon: {selected ammo type, reload time} from the previous tick. */
    private static final Map<VehicleEntity, Map<String, int[]>> STATE = new WeakHashMap<>();

    private ReloadSoundStopper() {}

    public static void tick(VehicleEntity vehicle) {
        if (!(vehicle.level() instanceof ServerLevel level)) return;
        Map<String, int[]> last = STATE.computeIfAbsent(vehicle, v -> new HashMap<>());
        for (Map.Entry<String, GunData> e : vehicle.getGunDataMap().entrySet()) {
            GunData data = e.getValue();
            int type = data.selectedAmmoType.get(), reload = data.reload.time();
            int[] prev = last.get(e.getKey());
            if (prev != null && prev[0] != type && prev[1] > 0) stopReloadSounds(level, vehicle, data);
            last.put(e.getKey(), new int[]{type, reload});
        }
    }

    private static void stopReloadSounds(ServerLevel level, VehicleEntity vehicle, GunData data) {
        SoundInfo info = data.get(GunProp.SOUND_INFO);
        stop(level, vehicle, info.vehicleReload);
        stop(level, vehicle, info.getVehicleReload3p());
    }

    private static void stop(ServerLevel level, VehicleEntity vehicle, SoundEvent sound) {
        if (sound == null) return;
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(sound.getLocation(), SoundSource.PLAYERS);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(vehicle) <= RANGE * RANGE) player.connection.send(packet);
        }
    }
}
