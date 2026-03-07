package frontline.combat.fcp.entity.vehicle.GazTigr;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class GazTigrEntity extends CamoVehicleBase {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/gaz_tigr/gaz_tigr_1.png"),
            new ResourceLocation("fcp", "textures/entity/gaz_tigr/gaz_tigr_2.png")
    };

    private static final String[] CAMO_NAMES = {"Standard", "Camo"};

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(GazTigrEntity.class, EntityDataSerializers.FLOAT);

    private float prevSteeringAngle = 0f;
    private float wheelRotation = 0f;
    private float prevWheelRotation = 0f;

    public GazTigrEntity(EntityType<GazTigrEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public ResourceLocation[] getCamoTextures() {
        return CAMO_TEXTURES;
    }

    @Override
    public String[] getCamoNames() {
        return CAMO_NAMES;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STEERING_ANGLE, 0f);
    }

    public float getSteeringAngle() {
        return this.entityData.get(STEERING_ANGLE);
    }

    public void setSteeringAngle(float angle) {
        this.entityData.set(STEERING_ANGLE, angle);
    }

    public float getPrevSteeringAngle() {
        return prevSteeringAngle;
    }

    public float getWheelRotation() {
        return wheelRotation;
    }

    public float getPrevWheelRotation() {
        return prevWheelRotation;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SteeringAngle", getSteeringAngle());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SteeringAngle")) {
            setSteeringAngle(compound.getFloat("SteeringAngle"));
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();

        prevSteeringAngle = getSteeringAngle();
        float currentAngle = getSteeringAngle();

        // Проверяем движется ли машина
        double speed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x +
                this.getDeltaMovement().z * this.getDeltaMovement().z);
        boolean isMoving = speed > 0.05;

        // Ловим нажатие клавиш A/D напрямую
        boolean turningLeft = this.leftInputDown();
        boolean turningRight = this.rightInputDown();

        // Если жмём клавиши поворота - крутим колёса
        if (turningLeft && !turningRight) {
            currentAngle += 2.0f;
            currentAngle = Math.min(45f, currentAngle);
            setSteeringAngle(currentAngle);
        } else if (turningRight && !turningLeft) {
            currentAngle -= 2.0f;
            currentAngle = Math.max(-45f, currentAngle);
            setSteeringAngle(currentAngle);
        } else if (isMoving && Math.abs(currentAngle) > 0.5f) {
            // Быстрое центрирование при движении
            currentAngle *= 0.9f;
            setSteeringAngle(currentAngle);
        }
        // Если стоим - колёса остаются на месте!

        // Если машина движется и колёса повёрнуты - поворачиваем машину
        if (isMoving && Math.abs(currentAngle) > 1f) {
            float turnAmount = currentAngle * 0.008f * (float)speed;
            this.setYRot(this.getYRot() + turnAmount);
        }

        // Вращение колёс на основе скорости движения
        prevWheelRotation = wheelRotation;
        wheelRotation += (float) (speed * 20);
    }
}
