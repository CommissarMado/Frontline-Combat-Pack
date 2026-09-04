package frontline.combat.fcp.entity.vehicle.M109;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoArtilleryBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class M109Entity extends CamoArtilleryBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(M109Entity.class, EntityDataSerializers.FLOAT);

    private float prevSteeringAngle = 0f;

    private float wheelRotation = 0f;

    private float prevWheelRotation = 0f;

    public M109Entity(EntityType<M109Entity> type, Level world) {super(type, world);}

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STEERING_ANGLE, 0f);
    }

    public float getSteeringAngle() {
        return this.entityData.get(STEERING_ANGLE);
    }

    public void setSteeringAngle(float angle) {this.entityData.set(STEERING_ANGLE, angle);
    }

    public float getPrevSteeringAngle(){
        return prevSteeringAngle;
    }

    public float getWheelRotation(){
        return wheelRotation;
    }

    public float getPrevWheelRotation(){
        return prevWheelRotation;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SteeringAngle", this.getSteeringAngle());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SteeringAngle")) {
            setSteeringAngle(compound.getFloat("SteeringAngle"));
        }
    }
    public boolean GetWeaponState(String WeaponName, int Count) {
        if (getAmmoCount(WeaponName) == Count)
            return true;
        else if (getAmmoCount(WeaponName) < Count)
            return true;
        else
            return false;

    }

    @Override
    public void baseTick() {
        super.baseTick();

        prevSteeringAngle = getSteeringAngle();
        float currentAngle = getSteeringAngle();

        double speed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);

        boolean isMoving = speed > 0.05;

        boolean turningLeft = this.leftInputDown();
        boolean turningRight = this.rightInputDown();

        if (turningLeft && !turningRight) {
            currentAngle += 2.0f;
            currentAngle = Math.min(45f, currentAngle);
            setSteeringAngle(currentAngle);
        } else if (turningRight && !turningLeft) {
            currentAngle -= 2.0f;
            currentAngle = Math.max(-45f, currentAngle);
            setSteeringAngle(currentAngle);
        } else if (isMoving && Math.abs(currentAngle) > 0.5f) {
            currentAngle *= 0.9f;
            setSteeringAngle(currentAngle);
        }

        if (isMoving && Math.abs(currentAngle) > 1f) {
            float turnAmount = currentAngle * 0.009f * (float)speed;
            this.setYRot(this.getYRot() + turnAmount);
        }

        // Track steering: this vehicle turns via the yaw steering above, so SBW's trackEngine
        // differential (which comes from its own turn rate, deltaRot) never engages and both
        // tracks scroll identically. Drive the visible differential from the steering angle here:
        // outer track scrolls faster, inner slower. TRACK_STEER_DIFF is the tuning knob; flip its
        // sign if the tracks steer the wrong way, raise it for a stronger effect.
        // The road wheels take the SAME-sign offset as the tracks: leftTrack/leftWheelRot share
        // SBW's sign convention, and the wheel render already agrees with the tracks for forward
        // travel, so the same structure differentiates them the same way. WHEEL_STEER_DIFF is a
        // separate magnitude (wheels spin at a different rate than the track scrolls) — tune it so
        // the wheels visually match the track differential.
        final float TRACK_STEER_DIFF = 0.15f;
        final float WHEEL_STEER_DIFF = 0.05f;
        if (isMoving && Math.abs(currentAngle) > 1f) {
            float dTrack = currentAngle * TRACK_STEER_DIFF * (float) speed;
            setLeftTrack(getLeftTrack() - dTrack);
            setRightTrack(getRightTrack() + dTrack);

            // Wheels render mirrored (left = -1.5*leftWheelRot, right = +1.5*rightWheelRot), so
            // SAME-sign offsets here become OPPOSITE visual rotation = the differential. (Opposite
            // signs cancelled and both wheels moved together.) If the differential ends up on the
            // wrong side, flip both to + dWheel.
            float dWheel = currentAngle * WHEEL_STEER_DIFF * (float) speed;
            setLeftWheelRot(getLeftWheelRot() - dWheel);
            setRightWheelRot(getRightWheelRot() + dWheel);
        }

        prevWheelRotation = wheelRotation;
        wheelRotation += (float) (speed * 20f);
    }
}