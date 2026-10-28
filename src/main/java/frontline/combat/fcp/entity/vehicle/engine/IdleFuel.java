package frontline.combat.fcp.entity.vehicle.engine;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;

/**
 * A running ground-vehicle engine burns fuel even standing still. SBW only charges for throttle, so
 * this adds an idle burn of {@link #IDLE_SHARE} of the full-throttle rate (a diesel idling uses
 * roughly a tenth of its full-load fuel flow) whenever the engine runs: started, for vehicles with
 * engine start/stop, otherwise while someone is in the driver's seat.
 */
public final class IdleFuel {

    private static final double IDLE_SHARE = 0.1;

    private IdleFuel() {
    }

    /** Server tick; returns the fractional energy carried to the next tick. */
    public static double tick(VehicleEntity vehicle, double carry) {
        if (vehicle.level().isClientSide() || vehicle.isWreck() || !vehicle.hasEnergyStorage() || vehicle.getEnergy() <= 0) return carry;
        EngineType type = vehicle.computed().getEngineType();
        if (type != EngineType.WHEEL && type != EngineType.TRACK) return carry;
        boolean running = EngineControl.appliesTo(vehicle)
                ? EngineControl.state(vehicle) == EngineControl.RUNNING
                : vehicle.getFirstPassenger() != null;
        if (!running) return carry;

        carry += vehicle.getEngineInfo().getEnergyCostRate() * IDLE_SHARE;
        int whole = (int) carry;
        if (whole > 0) vehicle.consumeEnergy(whole);
        return carry - whole;
    }
}
