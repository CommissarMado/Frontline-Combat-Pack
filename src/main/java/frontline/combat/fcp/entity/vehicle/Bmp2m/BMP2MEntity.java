package frontline.combat.fcp.entity.vehicle.Bmp2m;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4d;

public class BMP2MEntity extends CamoVehicleBase {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_1_1_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_1_afgan.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2_ukr_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2_ukr_2_kom_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2_ukr_2_kom_2.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2_ukr_2.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_fin_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_zov_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_zov_2.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1_gdr_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1_gdr_2.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1_gdr_3.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1_kom_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_2.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_3_v.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_3.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_4.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_5.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_c_kom_1.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_1_1_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_1_afgan_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2_ukr_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2_ukr_2_kom_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2_ukr_2_kom_2_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2_ukr_2_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_2_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_fin_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_zov_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_zov_2_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1_gdr_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1_gdr_2_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1_gdr_3_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1_kom_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_1_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_2_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_3_v_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_3_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_4_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_3_rus_5_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/bmp1_2/bmp_2_rem_tex_c_kom_1_wrecked.png")
    };

    private static final String[] CAMO_NAMES = {"T1 1 1", "T1 Afgan", "T1", "T2 Ukr 1", "T2 Ukr 2 Kom 1", "T2 Ukr 2 Kom 2", "T2 Ukr 2", "T2", "T3 Fin 1", "T3 Rus Zov 1", "T3 Rus Zov 2", "T3 Rus 1 Gdr 1", "T3 Rus 1 Gdr 2", "T3 Rus 1 Gdr 3", "T3 Rus 1 Kom 1", "T3 Rus 1", "T3 Rus 2", "T3 Rus 3 V", "T3 Rus 3", "T3 Rus 4", "T3 Rus 5", "Tc Kom 1"};

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(BMP2MEntity.class, EntityDataSerializers.FLOAT);

    private float prevSteeringAngle = 0f;

    private float wheelRotation = 0f;

    private float prevWheelRotation = 0f;

    public BMP2MEntity(EntityType<BMP2MEntity> type, Level world) {super(type, world);}

    @Override
    public ResourceLocation[] getCamoTextures() {return CAMO_TEXTURES;}

    @Override
    public String[] getCamoNames() {return CAMO_NAMES;}

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
                .custom((source, damage) -> getSourceAngle(source, 0.4f) * damage);
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

        prevWheelRotation = wheelRotation;
        wheelRotation += (float) (speed * 20f);
    }

    /**
     * The AGS-30 grenade launcher sits on the rear of the turret, on its own pivot, so it
     * cannot ride the "Barrel" transform - firing from Barrel would trace the arc of the
     * 2A42 trunnion instead of the launcher's own mount.
     *
     * This registers an extra "AGS" transform built exactly like SuperbWarfare's Barrel
     * transform (turret transform -> translate to the mount -> pitch), but anchored on the
     * AGS bone pivot. Weapons using "Transform": "AGS" therefore fire from the launcher and
     * track it as it elevates.
     *
     * Offset is the AGS bone pivot relative to the turret pivot, in the same units and sign
     * convention as TurretPos/BarrelPos (geo pixels / 16, Z negated).
     */
    private static final double AGS_X = 0.198577;
    private static final double AGS_Y = 0.622469;
    private static final double AGS_Z = -1.119981;

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
