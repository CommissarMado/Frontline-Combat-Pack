package frontline.combat.fcp.entity.vehicle.engine;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Engine start/stop for FCP land vehicles.
 *
 * Starting is a hold: while the engine is off, holding the engine key cranks it (STARTING, the
 * start sound plays) and it catches once the vehicle's start time has passed (RUNNING). Letting
 * go early aborts back to OFF. Stopping is a single press while running and is instant.
 *
 * The client reports the held key every tick ({@link #holdStart}); the server aborts a start that
 * stops getting those. The state is a synced byte on CamoVehicleBase / CamoArtilleryBase (see
 * {@link EngineControlled}) so the HUD bar and the engine sound player can read it client-side.
 *
 * Applies only to the vehicles in {@link #START_TICKS} with a Wheel, Track or Helicopter engine.
 * Helicopters only hold for the ignition (HELICOPTER_HOLD_TICKS); the rotor spool-up then runs on.
 */
public final class EngineControl {

    public static final byte OFF = 0;
    public static final byte STARTING = 1;
    public static final byte RUNNING = 2;

    /**
     * Vehicles with engine start/stop, and how long each takes to start: the length of its
     * Squad start sound as the driver hears it (interior), in ticks. Generated from the SDK start
     * cues; vehicles not in Squad use the closest Squad vehicle.
     */
    public static final Map<String, Integer> START_TICKS = startTicks();

    private static Map<String, Integer> startTicks() {
        Map<String, Integer> m = new java.util.HashMap<>();
        // btr80: start_int
        put(m, 49, "fcp:btr80", "fcp:btr80_cope", "fcp:btr82", "fcp:btr82_cope", "fcp:btr82at", "fcp:btr3e", "fcp:btr4mv1", "fcp:btr4e1", "fcp:btr4e");
        // aavp7: aavp7a1_engine_start_int_01
        put(m, 61, "fcp:aavp", "fcp:m109");
        // bmp1: bmp1_engine_start_int_01
        put(m, 51, "fcp:bmp1", "fcp:bmp1u", "fcp:bmp1am", "fcp:bmp1p");
        // bmp2: bmp2_engine_start_int_01
        put(m, 55, "fcp:bmp2", "fcp:bmp2d", "fcp:bmp2m", "fcp:bmp2_noatgm", "fcp:bmp2md");
        // warrior (Puma): warrior_engine_int_start_01, 1.13 s
        put(m, 23, "fcp:puma", "fcp:puma_atgm");
        // bmp3 (Khrizantema-S, BMP-3 family): bmp3m_engine_start_int_01, 5.21 s
        put(m, 104, "fcp:khrizantema", "fcp:bmp3", "fcp:bmp3_cage", "fcp:bmp3m", "fcp:brm3k");
        // brdm2: bdrm_int_start_01
        put(m, 72, "fcp:brdm2");
        // lav25: lav25_engine_start_int_01
        put(m, 31, "fcp:lav25");
        // stryker: stryker_engine_int_start_01
        put(m, 41, "fcp:stryker_mgs", "fcp:stryker_m2", "fcp:stryker_dragoon", "fcp:stryker_mk19", "fcp:stryker_tow", "fcp:stryker_mortar");
        // t72: t72_engine_start_int_01
        put(m, 49, "fcp:t72av", "fcp:msta");
        // abrams: abrams_engine_int_start_01 (gas turbine spool-up)
        put(m, 261, "fcp:m1a1", "fcp:m1a1_aim", "fcp:m1a1_ukr", "fcp:m1a2", "fcp:m1a2_tusk1", "fcp:m1a2_tusk2");
        // t90: t90_engine_start_int_01
        put(m, 33, "fcp:t14_armata");
        // technical: technical_engine_start_int_01
        put(m, 47, "fcp:toyota_hilux", "fcp:toyota_hilux_rocket_pod", "fcp:toyota_hilux_bmp", "fcp:toyota_hilux_spg9", "fcp:toyota_hilux_mortar", "fcp:toyota_hilux_zu23");
        // mgator: mgator_engine_start_ext_01
        put(m, 25, "fcp:dpv_m240", "fcp:dpv_minigun");
        // csk131: csk_engine_start_int_01
        put(m, 29, "fcp:uaz", "fcp:uaz_dshka", "fcp:uaz_spg9", "fcp:uaz_452", "fcp:uaz_3303");
        // ural4320: ural2_engine_int_start_01
        put(m, 47, "fcp:ural", "fcp:ural_fuel", "fcp:ural_kung", "fcp:ural_grad", "fcp:john_deere", "fcp:combine");
        // ural375: ural_engine_start_int
        put(m, 48, "fcp:gaz66", "fcp:zis5");
        // kamaz: 5350kamaz_engine_start_int_01
        put(m, 80, "fcp:kamaz", "fcp:kamaz_kung", "fcp:kamaz_long", "fcp:typhoon_vdv", "fcp:typhoon_vdv_unarmed", "fcp:pantsir");
        // tigr: tigr_engine_start_int_01
        put(m, 28, "fcp:gaz_tigr", "fcp:gaz_tigr_rws", "fcp:gaz_tigr_mg", "fcp:gaz_tigr_gl", "fcp:gaz_tigr_dual");
        // matv: matv_engine_int_start_01
        put(m, 39, "fcp:matv", "fcp:matv_tow", "fcp:matv_crow", "fcp:matv_9in1", "fcp:novator", "fcp:novator_unarmed", "fcp:kozak5", "fcp:kozak2m1", "fcp:kozak_ambulance");
        // us_truck: us_truck_engine_start_int_01 (FMTV; also the RG-33 and Caimans, which need a heavier engine)
        put(m, 53, "fcp:m939", "fcp:fmtv", "fcp:fmtv_4x4",
                "fcp:rg_33", "fcp:caiman", "fcp:caiman_unarmed", "fcp:caiman_police", "fcp:caiman_uparmored", "fcp:caiman_uparmored_unarmed");
        // man_truck: us_truck_engine_start_int_01
        put(m, 53, "fcp:man_hx58", "fcp:man_hx58_mg3", "fcp:man_hx60");
        // gwagon: gwagon_engine_start_int_01
        put(m, 30, "fcp:landrover_110");
        // m1151: m1151_start_int_01
        put(m, 70, "fcp:hmmwv_ambulance", "fcp:hmmwv_armored_m2", "fcp:hmmwv_armored_mk19", "fcp:hmmwv_armored_unarmed", "fcp:hmmwv_asrad", "fcp:hmmwv_avenger", "fcp:hmmwv_cargo", "fcp:hmmwv_shelter", "fcp:hmmwv_soft_top", "fcp:hmmwv_soft_top_no_doors", "fcp:toyota_hmv", "fcp:hmmwv_unarmored_m2", "fcp:hmmwv_unarmored_m2_shield", "fcp:hmmwv_unarmored_m2_turret", "fcp:hmmwv_unarmored_tow", "fcp:hmmwv_unarmored_tow_turret", "fcp:hmmwv_unarmored_unarmed");
        // Helicopters: Squad rotor start (int), 20 s. Only HELICOPTER_HOLD_TICKS of it is a hold.
        put(m, 400, "fcp:littlebird", "fcp:littlebird_armed", "fcp:littlebird_heavy", "fcp:bigbird", "fcp:oh1");
        put(m, 400, "fcp:uh60", "fcp:uh60_minigun", "fcp:mh60l");
        put(m, 400, "fcp:huey", "fcp:huey_m60_door_guns", "fcp:huey_m60_gunship", "fcp:huey_m134_door_guns", "fcp:huey_m134_gunship");
        put(m, 400, "fcp:venom", "fcp:venom_gunship", "fcp:venom_door_guns", "fcp:viper");
        put(m, 400, "fcp:mi8", "fcp:mi17", "fcp:mi8_door_guns", "fcp:mi17_door_guns", "fcp:mi8_mtv", "fcp:mi8_amtsh", "fcp:ch53a", "fcp:ch53e", "fcp:mh53");
        // ch47: SPM mod CH-47D start (Startup_INT_APU__001_FADE), 23.1 s
        put(m, 462, "fcp:ch47");
        // cpv: cpv_engine_start_int (generic SUV)
        put(m, 50, "fcp:generic_suv");
        return Map.copyOf(m);
    }

    private static void put(Map<String, Integer> m, int ticks, String... ids) {
        for (String id : ids) m.put(id, ticks);
    }

    /** A start aborts if the hold stops being reported for longer than this. */
    private static final int HOLD_TIMEOUT = 3;

    /**
     * Helicopters: the rotor spool-up (~20 s) is far too long to hold a key through, so only this
     * much is a hold (the ignition); after it the start runs on by itself.
     */
    public static final int HELICOPTER_HOLD_TICKS = 20;

    /**
     * Vehicles whose engine is voiced by FCP's Squad engine player instead of SBW's EngineSound.
     * Filled client-side by the profile loader; stays empty on a dedicated server.
     */
    public static final Set<String> SQUAD_SOUND_VEHICLES = ConcurrentHashMap.newKeySet();

    private static final Map<VehicleEntity, Integer> STARTED_AT = new WeakHashMap<>();
    private static final Map<VehicleEntity, Integer> LAST_HOLD = new WeakHashMap<>();

    private EngineControl() {
    }

    public static String id(VehicleEntity vehicle) {
        return EntityType.getKey(vehicle.getType()).toString();
    }

    public static boolean appliesTo(VehicleEntity vehicle) {
        if (!(vehicle instanceof EngineControlled) || !START_TICKS.containsKey(id(vehicle))) return false;
        EngineType type = vehicle.computed().getEngineType();
        return type == EngineType.WHEEL || type == EngineType.TRACK || type == EngineType.HELICOPTER;
    }

    public static boolean isHelicopter(VehicleEntity vehicle) {
        return vehicle.computed().getEngineType() == EngineType.HELICOPTER;
    }

    /** Ticks the key must be held for a start to commit: the whole start, or the ignition on helicopters. */
    public static int holdTicks(VehicleEntity vehicle) {
        return isHelicopter(vehicle) ? Math.min(HELICOPTER_HOLD_TICKS, startTicks(vehicle)) : startTicks(vehicle);
    }

    /** No fuel/energy left: the engine can't start, and a running one dies. */
    public static boolean outOfFuel(VehicleEntity vehicle) {
        return vehicle.hasEnergyStorage() && vehicle.getEnergy() <= 0;
    }

    public static int startTicks(VehicleEntity vehicle) {
        return START_TICKS.getOrDefault(id(vehicle), 40);
    }

    public static byte state(VehicleEntity vehicle) {
        return vehicle instanceof EngineControlled c ? c.fcp$getEngineState() : RUNNING;
    }

    /** True when drive inputs must be ignored. */
    public static boolean blocksInput(VehicleEntity vehicle) {
        return appliesTo(vehicle) && state(vehicle) != RUNNING;
    }

    /** True when SBW's own engine loop should be silent because a Squad profile replaces it. */
    public static boolean usesSquadSound(VehicleEntity vehicle) {
        return !SQUAD_SOUND_VEHICLES.isEmpty() && SQUAD_SOUND_VEHICLES.contains(id(vehicle));
    }

    /** Server: the driver is holding the engine key. Begins a start from OFF, keeps one going. */
    public static void holdStart(VehicleEntity vehicle) {
        if (!appliesTo(vehicle) || vehicle.isWreck()) return;
        EngineControlled c = (EngineControlled) vehicle;
        byte state = c.fcp$getEngineState();
        if (state == OFF) {
            if (outOfFuel(vehicle)) return;
            c.fcp$setEngineState(STARTING);
            STARTED_AT.put(vehicle, vehicle.tickCount);
        }
        if (state != RUNNING) LAST_HOLD.put(vehicle, vehicle.tickCount);
    }

    /** Server: the driver pressed the engine key while it runs. Instant shutdown. */
    public static void stop(VehicleEntity vehicle) {
        if (!appliesTo(vehicle)) return;
        setOff(vehicle);
    }

    /** Helicopter idle: turns the rotor without lifting. (SBW spools to 0.12, which lifts some FCP helicopters.) */
    public static final float HELICOPTER_IDLE_POWER = 0.05f;
    /**
     * Engine-off wind-down: the rotor loses whatever power it had at shutdown over ~21.5 s, roughly
     * the length of Squad's rotor stop sounds (16-28 s). The time is shared by every helicopter and
     * does not depend on the power, so a rotor at idle (landed) keeps turning as long as one shut
     * down at full power - a fixed per-tick loss had idle rotors stop in about a second and a half.
     */
    private static final int HELICOPTER_SPIN_DOWN_TICKS = 430;
    /** Per-tick power loss for the current wind-down, fixed from the power at shutdown. */
    private static final Map<VehicleEntity, Float> SPIN_DOWN = new WeakHashMap<>();

    /**
     * Server: SBW turns the rotor from its power value, so power follows our engine state - it
     * spools up to idle over the start, never drops below idle on the ground while running (the
     * rotor keeps turning after landing), and winds down to nothing once the engine is off instead
     * of staying wherever the throttle was left.
     */
    private static void rotorPower(VehicleEntity vehicle, byte state) {
        float power = vehicle.getPower();
        if (state == STARTING) {
            Integer started = STARTED_AT.get(vehicle);
            float progress = started == null ? 0f : Math.min(1f, (vehicle.tickCount - started) / (float) startTicks(vehicle));
            vehicle.setPower(HELICOPTER_IDLE_POWER * progress);
        } else if (state == RUNNING) {
            if (vehicle.onGround() && power < HELICOPTER_IDLE_POWER) vehicle.setPower(HELICOPTER_IDLE_POWER);
        } else if (power != 0f) {
            float step = SPIN_DOWN.computeIfAbsent(vehicle, v -> Math.abs(power) / HELICOPTER_SPIN_DOWN_TICKS);
            vehicle.setPower(power > 0 ? Math.max(0f, power - step) : Math.min(0f, power + step));
            return;
        }
        SPIN_DOWN.remove(vehicle);
    }

    private static void setOff(VehicleEntity vehicle) {
        ((EngineControlled) vehicle).fcp$setEngineState(OFF);
        STARTED_AT.remove(vehicle);
        LAST_HOLD.remove(vehicle);
    }

    /**
     * Tick (both sides): keeps SBW's helicopter start flags in step with ours; server-side also
     * finishes or aborts a start and kills the engine on a wreck or an empty tank.
     */
    public static void tick(VehicleEntity vehicle) {
        if (!(vehicle instanceof EngineControlled c) || !appliesTo(vehicle)) return;
        byte state = c.fcp$getEngineState();
        if (isHelicopter(vehicle)) {
            // SBW spools a helicopter up when the pilot presses up with its engine off; ours replaces
            // that start, so SBW's flags simply mirror our state (inputs are blocked until RUNNING).
            boolean running = state == RUNNING;
            if (vehicle.getEngineStart() != running) vehicle.setEngineStart(running);
            if (vehicle.getEngineStartOver() != running) vehicle.setEngineStartOver(running);
        }
        if (vehicle.level().isClientSide()) return;
        if (isHelicopter(vehicle)) rotorPower(vehicle, state);
        if (state == OFF) return;
        if (vehicle.isWreck() || (state == RUNNING && outOfFuel(vehicle))) {
            setOff(vehicle);
            return;
        }
        if (state == STARTING) {
            Integer started = STARTED_AT.get(vehicle);
            Integer held = LAST_HOLD.get(vehicle);
            // No record (loaded mid-start) or the key was let go: the start fails.
            boolean released = held == null || vehicle.tickCount - held > HOLD_TIMEOUT;
            boolean committed = started != null && vehicle.tickCount - started >= holdTicks(vehicle);
            if (started == null || (released && !committed)) {
                setOff(vehicle);
            } else if (vehicle.tickCount - started >= startTicks(vehicle)) {
                c.fcp$setEngineState(RUNNING);
                STARTED_AT.remove(vehicle);
                LAST_HOLD.remove(vehicle);
            }
        }
    }
}
