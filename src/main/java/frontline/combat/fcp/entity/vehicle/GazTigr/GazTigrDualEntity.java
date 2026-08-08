package frontline.combat.fcp.entity.vehicle.GazTigr;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Matrix4d;

public class GazTigrDualEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final ResourceLocation[] CAMO_TEXTURES = {
            //Normal Texture
            new ResourceLocation("fcp", "textures/entity/gaz_tigr/gaz_tigr_1.png"),
            new ResourceLocation("fcp", "textures/entity/gaz_tigr/gaz_tigr_2.png"),
            //Wrecked Texture
            new ResourceLocation("fcp", "textures/entity/gaz_tigr/gaz_tigr_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/gaz_tigr/gaz_tigr_2_wrecked.png")
    };

    private static final String[] CAMO_NAMES = {"Standard", "Camo"};

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(GazTigrDualEntity.class, EntityDataSerializers.FLOAT);

    private float prevSteeringAngle = 0f;
    private float wheelRotation = 0f;
    private float prevWheelRotation = 0f;

    public GazTigrDualEntity(EntityType<GazTigrDualEntity> type, Level world) {
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

        double speed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x +
                this.getDeltaMovement().z * this.getDeltaMovement().z);
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
            float turnAmount = currentAngle * 0.008f * (float) speed;
            this.setYRot(this.getYRot() + turnAmount);
        }

        prevWheelRotation = wheelRotation;
        wheelRotation += (float) (speed * 20);
    }

    /**
     * The AGS sits on its own trunnion (bone "barrel"), separate from the PKM which rides the
     * standard "Barrel" transform (so the gunner camera elevates via SuperbWarfare's native path).
     * Firing the AGS from "Barrel" would trace the PKM mount's arc instead of the AGS's, so this
     * registers an extra "AGS" transform built exactly like SuperbWarfare's Barrel transform
     * (turret transform -> translate to the mount -> pitch), anchored on the AGS bone pivot.
     * Weapons using "Transform": "AGS" therefore fire from the AGS and track it as it elevates.
     *
     * Offset is the AGS bone ("barrel") pivot relative to the turret pivot, in the same units and
     * sign convention as TurretPos/BarrelPos (geo pixels / 16, Z negated).
     */
    private static final double AGS_X = -0.5084038;
    private static final double AGS_Y = 0.0236025;
    private static final double AGS_Z = -0.13904;

    @Override
    public Matrix4d getTransformFromString(String string, float ticks) {
        if ("AGS".equals(string)) {
            Matrix4d transform = this.getTurretTransform(ticks);
            transform.translate(AGS_X, AGS_Y, AGS_Z);
            transform.rotate(Axis.XP.rotationDegrees(Mth.lerp(ticks, this.getTurretXRotO(), this.getTurretXRot())));
            return transform;
        }
        return super.getTransformFromString(string, ticks);
    }
}
