package frontline.combat.fcp.entity.vehicle.Pantsir;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PantsirEntity extends CamoVehicleBase {
    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/kamaz/kamaz.png"),
            new ResourceLocation("fcp", "textures/entity/kamaz/kamaz.png")
    };
    private static final String[] CAMO_NAMES = {"Standard"};
    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(PantsirEntity.class, EntityDataSerializers.FLOAT);
    private float prevSteeringAngle = 0f;
    private float wheelRotation = 0f;
    private float prevWheelRotation = 0f;
    public PantsirEntity(EntityType<PantsirEntity> type, Level world) {super(type, world);}
    @Override public ResourceLocation[] getCamoTextures() {return CAMO_TEXTURES;}
    @Override public String[] getCamoNames() {return CAMO_NAMES;}
    @Override protected void defineSynchedData() {super.defineSynchedData(); this.entityData.define(STEERING_ANGLE, 0f);}
    public float getSteeringAngle() {return this.entityData.get(STEERING_ANGLE);}
    public void setSteeringAngle(float a) {this.entityData.set(STEERING_ANGLE, a);}
    public float getPrevSteeringAngle(){return prevSteeringAngle;}
    public float getWheelRotation(){return wheelRotation;}
    public float getPrevWheelRotation(){return prevWheelRotation;}
    @Override public DamageModifier getDamageModifier() {return super.getDamageModifier().custom((s,dmg) -> getSourceAngle(s, 0.4f) * dmg);}
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag c) {super.addAdditionalSaveData(c); c.putFloat("SteeringAngle", getSteeringAngle());}
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag c) {super.readAdditionalSaveData(c); if (c.contains("SteeringAngle")) setSteeringAngle(c.getFloat("SteeringAngle"));}
    @Override public void baseTick() {
        super.baseTick();
        prevSteeringAngle = getSteeringAngle();
        float a = getSteeringAngle();
        double sp = Math.sqrt(this.getDeltaMovement().x*this.getDeltaMovement().x + this.getDeltaMovement().z*this.getDeltaMovement().z);
        boolean mv = sp > 0.05;
        if (this.leftInputDown() && !this.rightInputDown()) {a=Math.min(45f,a+2.0f); setSteeringAngle(a);}
        else if (this.rightInputDown() && !this.leftInputDown()) {a=Math.max(-45f,a-2.0f); setSteeringAngle(a);}
        else if (mv && Math.abs(a)>0.5f) {a*=0.9f; setSteeringAngle(a);}
        if (mv && Math.abs(a)>1f) this.setYRot(this.getYRot() + a*0.009f*(float)sp);
        prevWheelRotation = wheelRotation;
        double fwd = this.getDeltaMovement().x*Math.sin(Math.toRadians(this.getYRot())) + this.getDeltaMovement().z*Math.cos(Math.toRadians(this.getYRot()));
        if (fwd>0) wheelRotation += (float)(sp*20f); else if (fwd<0) wheelRotation -= (float)(sp*20f);
    }
}
