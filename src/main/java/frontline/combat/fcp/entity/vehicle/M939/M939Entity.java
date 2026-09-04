package frontline.combat.fcp.entity.vehicle.M939;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class M939Entity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(M939Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> TENT = SynchedEntityData.defineId(M939Entity.class, EntityDataSerializers.BOOLEAN);
    private boolean tentInit = false;
    private float prevSteeringAngle = 0f;
    private float wheelRotation = 0f;
    private float prevWheelRotation = 0f;
    public M939Entity(EntityType<M939Entity> type, Level world) {super(type, world);}
    @Override protected void defineSynchedData() {super.defineSynchedData(); this.entityData.define(STEERING_ANGLE, 0f); this.entityData.define(TENT, true);}
    public boolean hasTent() {return this.entityData.get(TENT);}
    public void setTent(boolean v) {this.entityData.set(TENT, v);}
    public void toggleTent() {setTent(!hasTent());}
    public float getSteeringAngle() {return this.entityData.get(STEERING_ANGLE);}
    public void setSteeringAngle(float angle) {this.entityData.set(STEERING_ANGLE, angle);}
    public float getPrevSteeringAngle(){return prevSteeringAngle;}
    public float getWheelRotation(){return wheelRotation;}
    public float getPrevWheelRotation(){return prevWheelRotation;}
    @Override public DamageModifier getDamageModifier() {return super.getDamageModifier().custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);}
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {super.addAdditionalSaveData(compound); compound.putFloat("SteeringAngle", this.getSteeringAngle()); compound.putBoolean("Tent", hasTent()); compound.putBoolean("TentInit", tentInit);}
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {super.readAdditionalSaveData(compound); if (compound.contains("TentInit")) tentInit = compound.getBoolean("TentInit"); if (compound.contains("Tent")) setTent(compound.getBoolean("Tent")); if (compound.contains("SteeringAngle")) setSteeringAngle(compound.getFloat("SteeringAngle"));}
    @Override public void baseTick() {
        super.baseTick();
        // Randomise the tent once on first spawn (like the Ural), then persist it.
        if (!this.level().isClientSide() && !tentInit) {setTent(this.random.nextBoolean()); tentInit = true;}
        prevSteeringAngle = getSteeringAngle();
        float currentAngle = getSteeringAngle();
        double speed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);
        boolean isMoving = speed > 0.05;
        if (this.leftInputDown() && !this.rightInputDown()) {currentAngle = Math.min(45f, currentAngle + 2.0f); setSteeringAngle(currentAngle);}
        else if (this.rightInputDown() && !this.leftInputDown()) {currentAngle = Math.max(-45f, currentAngle - 2.0f); setSteeringAngle(currentAngle);}
        else if (isMoving && Math.abs(currentAngle) > 0.5f) {currentAngle *= 0.9f; setSteeringAngle(currentAngle);}
        if (isMoving && Math.abs(currentAngle) > 1f) this.setYRot(this.getYRot() + currentAngle * 0.009f * (float) speed);
        prevWheelRotation = wheelRotation;
        double fwd = this.getDeltaMovement().x * Math.sin(Math.toRadians(this.getYRot())) + this.getDeltaMovement().z * Math.cos(Math.toRadians(this.getYRot()));
        if (fwd > 0) wheelRotation += (float) (speed * 20f); else if (fwd < 0) wheelRotation -= (float) (speed * 20f);
    }
}
