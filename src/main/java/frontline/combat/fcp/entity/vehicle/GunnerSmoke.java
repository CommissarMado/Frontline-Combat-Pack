package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Gunner-fired smoke.
 *
 * <p>SBW sends the "release decoy" key as part of the driver's movement input, and only accepts that
 * input from the vehicle's first passenger - so the driver always pops smoke. On these vehicles the
 * smoke dischargers belong to the turret / weapon station, so here the decoy input comes from the
 * GUNNER instead (via GunnerSmokePacket) and the driver's key is ignored. In single-crew variants the
 * gunner is seat 0 anyway, so the same rule simply keeps working.
 */
public final class GunnerSmoke {

    private static final Set<String> GUNNER_SMOKE_VEHICLES = Set.of(
            // Had driver-fired smoke
            "fcp:aavp",
            "fcp:brdm2",
            "fcp:btr80",
            "fcp:btr80_cope",
            "fcp:btr82",
            "fcp:btr82_cope",
            "fcp:btr82at",
            "fcp:btr3e",
            "fcp:btr4mv1",
            "fcp:btr4e",
            "fcp:btr4e1",
            "fcp:lav25",
            "fcp:m109",
            "fcp:msta",
            "fcp:pantsir",
            // Smoke added
            "fcp:bmp1p",
            "fcp:bmp1am",
            "fcp:bmp1u",
            "fcp:bmp2",
            "fcp:puma",
            "fcp:puma_atgm",
            "fcp:bmp3",
            "fcp:bmp3_cage",
            "fcp:bmp3m",
            "fcp:brm3k",
            "fcp:bmp2_noatgm",
            "fcp:bmp2d",
            "fcp:bmp2m",
            "fcp:bmp2md",
            "fcp:t72av",
            "fcp:stryker_m2",
            "fcp:stryker_mk19",
            "fcp:stryker_mgs",
            "fcp:stryker_dragoon",
            "fcp:stryker_tow",
            "fcp:typhoon_vdv",
            "fcp:gaz_tigr_rws",
            "fcp:m1a1",
            "fcp:m1a1_aim",
            "fcp:m1a1_ukr",
            "fcp:m1a2",
            "fcp:m1a2_tusk1",
            "fcp:m1a2_tusk2"
    );

    /** Ticks the gunner's smoke input stays "down" after each GunnerSmokePacket (sent every tick while held). */
    private static final int HOLD_TICKS = 3;

    /** Server side: tick until which each vehicle's gunner smoke input counts as held. */
    private static final Map<VehicleEntity, Integer> HELD_UNTIL = new WeakHashMap<>();

    private GunnerSmoke() {
    }

    public static boolean isGunnerSmokeVehicle(VehicleEntity vehicle) {
        return GUNNER_SMOKE_VEHICLES.contains(EntityType.getKey(vehicle.getType()).toString());
    }

    /** True if this player is the one who fires this vehicle's smoke (its turret controller). */
    public static boolean isSmokeOperator(VehicleEntity vehicle, Player player) {
        return isGunnerSmokeVehicle(vehicle) && vehicle.getSeatIndex(player) == vehicle.getTurretControllerIndex();
    }

    /**
     * Whether this player's seat can actually release this vehicle's smoke / flares: the turret
     * controller on gunner-smoke vehicles, the driver (first passenger, SBW's rule) everywhere else.
     * Used to decide which HUDs show the "smoke ready / reloading" line.
     */
    public static boolean canFireSmoke(VehicleEntity vehicle, Player player) {
        if (!vehicle.hasDecoy()) {
            return false;
        }
        return isGunnerSmokeVehicle(vehicle) ? isSmokeOperator(vehicle, player) : vehicle.getFirstPassenger() == player;
    }

    /** GunnerSmokePacket: the gunner is holding the release-decoy key. */
    public static void hold(VehicleEntity vehicle) {
        HELD_UNTIL.put(vehicle, vehicle.tickCount + HOLD_TICKS);
    }

    /**
     * The decoy input to use for this vehicle, or null to keep SBW's normal (driver) input. Called from
     * the decoyInputDown() overrides in CamoVehicleBase and CamoArtilleryBase.
     */
    @Nullable
    public static Boolean decoyInput(VehicleEntity vehicle) {
        if (!isGunnerSmokeVehicle(vehicle)) {
            return null;
        }
        Integer until = HELD_UNTIL.get(vehicle);
        return until != null && vehicle.tickCount < until;
    }
}
