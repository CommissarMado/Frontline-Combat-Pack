package frontline.combat.fcp.entity.vehicle.engine;

/** Vehicles carrying the synced engine state used by {@link EngineControl}. */
public interface EngineControlled {

    byte fcp$getEngineState();

    void fcp$setEngineState(byte state);
}
