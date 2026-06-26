package frontline.combat.fcp.entity.vehicle;

/**
 * SteerableVehicle — marks a vehicle whose front wheels pivot for steering.
 *
 * Implement this on any vehicle that already exposes a steering angle (most do via
 * getSteeringAngle()/getPrevSteeringAngle()); declaring the interface is usually the
 * only change needed, since the methods already exist. WheelRotationTransforms reads
 * it to pivot "...Turn" wheel bones. Vehicles that don't implement it (trailers,
 * tracked vehicles) simply keep their wheels straight.
 *
 * Angles are in degrees; 0 is straight ahead, positive turns one way, negative the
 * other (the sign convention is whatever your model expects).
 */
public interface SteerableVehicle {

    /** Current steering angle in degrees (0 = straight). */
    float getSteeringAngle();

    /** Steering angle from the previous tick, for render interpolation. */
    float getPrevSteeringAngle();
}