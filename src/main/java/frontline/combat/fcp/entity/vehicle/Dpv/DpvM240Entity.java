package frontline.combat.fcp.entity.vehicle.Dpv;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DpvM240Entity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(DpvM240Entity.class, EntityDataSerializers.FLOAT);
    private float prevSteeringAngle = 0f;
    private float wheelRotation = 0f;
    private float prevWheelRotation = 0f;

    public DpvM240Entity(EntityType<DpvM240Entity> type, Level world) {super(type, world);}

    @Override
    protected void defineSynchedData() {super.defineSynchedData(); this.entityData.define(STEERING_ANGLE, 0f);}

    public float getSteeringAngle() {return this.entityData.get(STEERING_ANGLE);}
    public void setSteeringAngle(float angle) {this.entityData.set(STEERING_ANGLE, angle);}
    public float getPrevSteeringAngle() {return prevSteeringAngle;}
    public float getWheelRotation() {return wheelRotation;}
    public float getPrevWheelRotation() {return prevWheelRotation;}

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier().custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SteeringAngle", this.getSteeringAngle());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SteeringAngle")) setSteeringAngle(compound.getFloat("SteeringAngle"));
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
        if (turningLeft && !turningRight) {currentAngle += 2.0f; currentAngle = Math.min(45f, currentAngle); setSteeringAngle(currentAngle);}
        else if (turningRight && !turningLeft) {currentAngle -= 2.0f; currentAngle = Math.max(-45f, currentAngle); setSteeringAngle(currentAngle);}
        else if (isMoving && Math.abs(currentAngle) > 0.5f) {currentAngle *= 0.9f; setSteeringAngle(currentAngle);}
        if (isMoving && Math.abs(currentAngle) > 1f) {float turnAmount = currentAngle * 0.009f * (float) speed; this.setYRot(this.getYRot() + turnAmount);}
        prevWheelRotation = wheelRotation;
        wheelRotation += (float) (speed * 20f);
    }
}
