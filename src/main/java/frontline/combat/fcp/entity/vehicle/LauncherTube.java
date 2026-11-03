package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;

/**
 * Single-round ATGM launchers whose transport-launch container stays on the launcher after the shot (Novator Stugna,
 * Hilux Kornet): the spent tube is only taken off halfway through the reload, and the fresh one appears when it ends.
 * Server side (SBW's reload timer isn't synced); the vehicles sync the result.
 */
public final class LauncherTube {

    private LauncherTube() {
    }

    /** True from halfway through {@code weapon}'s empty reload until it's loaded again. */
    public static boolean off(VehicleEntity vehicle, String weapon) {
        GunData data;
        try {
            data = vehicle.getGunData(weapon);
        } catch (RuntimeException e) {
            return false;
        }
        if (data == null || !data.reloading() || vehicle.getAmmoCount(weapon) > 0) return false;
        // reload.time() counts down from EmptyReloadTime + 1 to 0.
        int total = data.get(GunProp.EMPTY_RELOAD_TIME).intValue();
        return data.reload.time() <= total / 2;
    }
}
