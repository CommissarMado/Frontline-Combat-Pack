package frontline.combat.fcp.entity.vehicle.Zis5;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Zis5Entity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(Zis5Entity.class, EntityDataSerializers.FLOAT);
    // Tent stage: 0 = no frame or canvas, 1 = frame only, 2 = frame + canvas.
    private static final EntityDataAccessor<Integer> TENT = SynchedEntityData.defineId(Zis5Entity.class, EntityDataSerializers.INT);

    private float prevSteeringAngle = 0f;
    private float wheelRotation = 0f;
    private float prevWheelRotation = 0f;

    public Zis5Entity(EntityType<Zis5Entity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STEERING_ANGLE, 0f);
        this.entityData.define(TENT, TENT_FULL);
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
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    private boolean tentInit = false;
    public static final int TENT_NONE = 0, TENT_FRAME = 1, TENT_FULL = 2;
    public int getTentStage() {return this.entityData.get(TENT);}
    public void setTentStage(int stage) {this.entityData.set(TENT, Math.floorMod(stage, 3));}
    public boolean hasTentFrame() {return getTentStage() >= TENT_FRAME;}
    public boolean hasTentCanvas() {return getTentStage() == TENT_FULL;}
    // Spray cycles frame + canvas -> frame only -> nothing -> frame + canvas.
    public void toggleTent() {setTentStage(getTentStage() - 1);}

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SteeringAngle", getSteeringAngle());
        compound.putInt("TentStage", getTentStage());
        compound.putBoolean("TentInit", tentInit);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TentInit")) tentInit = compound.getBoolean("TentInit");
        if (compound.contains("TentStage")) setTentStage(compound.getInt("TentStage"));
        else if (compound.contains("Tent")) setTentStage(compound.getBoolean("Tent") ? TENT_FULL : TENT_NONE);
        if (compound.contains("SteeringAngle")) {
            setSteeringAngle(compound.getFloat("SteeringAngle"));
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // Randomise the tent once on first spawn (like the humvee attachments), then persist it.
        if (!this.level().isClientSide() && !tentInit) {
            setTentStage(this.random.nextInt(3));
            tentInit = true;
        }

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
