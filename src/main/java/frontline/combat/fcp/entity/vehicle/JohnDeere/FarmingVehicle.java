package frontline.combat.fcp.entity.vehicle.JohnDeere;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;

/**
 * Farm machines with a farming on/off switch (the combine, the tractor). The driver seat carries a dummy weapon,
 * {@link #FARM_WEAPON}, with two ammo types - off and on, each with its own HUD icon - switched with SBW's own vehicle ammo
 * keys. While on, the machine works (the combine reaps, the tractor's implements till and sow) and its speed is capped to a
 * working speed; while off it drives at full road speed and does no work.
 */
public interface FarmingVehicle {

    /** Driver-seat weapon whose selected ammo type is the switch: 0 off, 1 on. */
    String FARM_WEAPON = "Farming";

    /** Whether a vehicle weapon name is one of these on/off switches (no ammo number on the HUD). */
    static boolean isSwitchWeapon(String name) {
        return FARM_WEAPON.equals(name) || CombineEntity.AUGER_WEAPON.equals(name);
    }

    /** Synced copy of the switch (the server reads it from the weapon each tick), for the clients' animations. */
    boolean isFarming();

    /** Share of full engine power allowed while farming: working speed / road speed. */
    float farmPowerCap();

    /** The farming switch as the driver's weapon has it (server). */
    static boolean switchedOn(VehicleEntity vehicle) {
        return switchedOn(vehicle, FARM_WEAPON);
    }

    /** An on/off switch weapon (two ammo types, second = on), e.g. the combine's auger (server). */
    static boolean switchedOn(VehicleEntity vehicle, String weapon) {
        GunData data = vehicle.getGunData(weapon);
        return data != null && data.selectedAmmoType.get() == 1;
    }

    /** Called after SBW's engine each tick: while farming, hold the engine power to the working-speed share. */
    static void capPower(VehicleEntity vehicle) {
        if (!(vehicle instanceof FarmingVehicle farm) || !farm.isFarming()) return;
        float cap = farm.farmPowerCap();
        float power = vehicle.getPower();
        if (Math.abs(power) > cap) vehicle.setPower(Math.copySign(cap, power));
    }
}
