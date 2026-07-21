package frontline.combat.fcp.entity.vehicle.Btr3e;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class BTR3EEntity extends CamoVehicleBase {
    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/btr3e/btr3e_green.png"),
            new ResourceLocation("fcp", "textures/entity/btr3e/btr3e_tan.png"),
            new ResourceLocation("fcp", "textures/entity/btr3e/btr3e_green_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/btr3e/btr3e_tan_wrecked.png")
    };
    private static final String[] CAMO_NAMES = {"Green", "Tan"};

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(BTR3EEntity.class, EntityDataSerializers.FLOAT);
    private float prevSteeringAngle = 0f, wheelRotation = 0f, prevWheelRotation = 0f;

    public BTR3EEntity(EntityType<BTR3EEntity> type, Level world) { super(type, world); }
    @Override public ResourceLocation[] getCamoTextures() { return CAMO_TEXTURES; }
    @Override public String[] getCamoNames() { return CAMO_NAMES; }
    @Override protected void defineSynchedData() { super.defineSynchedData(); this.entityData.define(STEERING_ANGLE, 0f); }
    public float getSteeringAngle() { return this.entityData.get(STEERING_ANGLE); }
    public void setSteeringAngle(float a) { this.entityData.set(STEERING_ANGLE, a); }
    public float getPrevSteeringAngle(){ return prevSteeringAngle; }
    public float getWheelRotation(){ return wheelRotation; }
    public float getPrevWheelRotation(){ return prevWheelRotation; }
    @Override public DamageModifier getDamageModifier() {
        return super.getDamageModifier().custom((source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag c) { super.addAdditionalSaveData(c); c.putFloat("SteeringAngle", getSteeringAngle()); }
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag c) { super.readAdditionalSaveData(c); if (c.contains("SteeringAngle")) setSteeringAngle(c.getFloat("SteeringAngle")); }
    @Override public void baseTick() {
        super.baseTick();
        prevSteeringAngle = getSteeringAngle();
        float a = getSteeringAngle();
        double speed = Math.sqrt(getDeltaMovement().x*getDeltaMovement().x + getDeltaMovement().z*getDeltaMovement().z);
        boolean moving = speed > 0.05;
        if (leftInputDown() && !rightInputDown()) { a = Math.min(45f, a+2f); setSteeringAngle(a); }
        else if (rightInputDown() && !leftInputDown()) { a = Math.max(-45f, a-2f); setSteeringAngle(a); }
        else if (moving && Math.abs(a) > 0.5f) { a *= 0.9f; setSteeringAngle(a); }
        if (moving && Math.abs(a) > 1f) setYRot(getYRot() + a*0.009f*(float)speed);
        prevWheelRotation = wheelRotation; wheelRotation += (float)(speed*20f);
    }
}
