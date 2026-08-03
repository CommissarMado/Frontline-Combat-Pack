package frontline.combat.fcp.entity.vehicle.Kozak;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class KozakAmbulanceEntity extends CamoVehicleBase {
    private static final ResourceLocation[] CAMO_TEXTURES = {
            //Normal
            new ResourceLocation("fcp", "textures/entity/kozak/kozak_ambu.png"),
            //Wrecked
            new ResourceLocation("fcp", "textures/entity/kozak/kozak_ambu.png")
    };
    private static final String[] CAMO_NAMES = {"Ambulance"};
    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(KozakAmbulanceEntity.class, EntityDataSerializers.FLOAT);
    private float prevSteeringAngle = 0f;
    private float wheelRotation = 0f;
    private float prevWheelRotation = 0f;
    public KozakAmbulanceEntity(EntityType<KozakAmbulanceEntity> type, Level world) {super(type, world);}
    @Override public ResourceLocation[] getCamoTextures() {return CAMO_TEXTURES;}
    @Override public String[] getCamoNames() {return CAMO_NAMES;}
    @Override protected void defineSynchedData() {super.defineSynchedData(); this.entityData.define(STEERING_ANGLE, 0f);}
    public float getSteeringAngle() {return this.entityData.get(STEERING_ANGLE);}
    public void setSteeringAngle(float angle) {this.entityData.set(STEERING_ANGLE, angle);}
    public float getPrevSteeringAngle(){return prevSteeringAngle;}
    public float getWheelRotation(){return wheelRotation;}
    public float getPrevWheelRotation(){return prevWheelRotation;}
    @Override public DamageModifier getDamageModifier() {return super.getDamageModifier().custom((source, damage) -> getSourceAngle(source, 0.4f) * damage);}
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {super.addAdditionalSaveData(compound); compound.putFloat("SteeringAngle", this.getSteeringAngle());}
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {super.readAdditionalSaveData(compound); if (compound.contains("SteeringAngle")) setSteeringAngle(compound.getFloat("SteeringAngle"));}
    @Override public void baseTick() {
        super.baseTick();
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
