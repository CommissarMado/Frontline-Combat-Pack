package frontline.combat.fcp.effects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Throttles muzzle VFX when a vehicle fires continuously at high RPM. */
final class MuzzleBurstTracker {

    private static final Map<Integer, State> STATES = new ConcurrentHashMap<>();
    private static final int GAP_RESET_TICKS = 18;
    private static final int SUSTAINED_SHOTS = 3;

    private MuzzleBurstTracker() {
    }

    static int recordShot(int vehicleId, long gameTick) {
        State state = STATES.computeIfAbsent(vehicleId, id -> new State());
        state.onShot(gameTick);
        return state.count;
    }

    static boolean isSustained(int vehicleId, long gameTick) {
        State state = STATES.get(vehicleId);
        return state != null && state.isSustained(gameTick);
    }

    static void clear(int vehicleId) {
        STATES.remove(vehicleId);
    }

    private static final class State {
        private long lastTick;
        private int count;

        private void onShot(long tick) {
            if (tick - lastTick > GAP_RESET_TICKS) {
                count = 0;
            }
            count++;
            lastTick = tick;
        }

        private boolean isSustained(long tick) {
            return count > SUSTAINED_SHOTS && tick - lastTick <= 2;
        }
    }
}
