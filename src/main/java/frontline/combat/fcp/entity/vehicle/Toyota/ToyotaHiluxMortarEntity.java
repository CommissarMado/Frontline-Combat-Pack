package frontline.combat.fcp.entity.vehicle.Toyota;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public class ToyotaHiluxMortarEntity extends CamoVehicleBase {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            //Normal Texture
            new ResourceLocation("fcp", "textures/entity/toyota/toyota_hilux_mortar.png"),
            //Wrecked Texture
            new ResourceLocation("fcp", "textures/entity/toyota/toyota_hilux_mortar_wrecked.png")
    };
    private static final String[] CAMO_NAMES = {"Toyota"};

    // Ticks between the fire sounds and the round leaving the tube (matches SBW's deployable
    // mortar: FIRE_TIME 25 -> spawn at 5). Tune to line the launch up with the boom.
    private static final int MORTAR_FIRE_DELAY = 20;

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(ToyotaHiluxMortarEntity.class, EntityDataSerializers.FLOAT);
    private float prevSteeringAngle = 0f, wheelRotation = 0f, prevWheelRotation = 0f;
    private int mortarFireTime = 0;
    private LivingEntity mortarShooter = null;
    private UUID mortarUuid = null;
    private Vec3 mortarTargetPos = null;

    public ToyotaHiluxMortarEntity(EntityType<ToyotaHiluxMortarEntity> type, Level world) { super(type, world); }

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

    // The fire packet routes through this 3-arg vehicleShoot. We intercept the gunner's shot:
    // play the shell-drop + fire sounds now, then launch the round MORTAR_FIRE_DELAY ticks later
    // so it leaves the tube on the boom instead of instantly. (Delaying every gunner shot is
    // fine — the mortar's only real weapon is the tube.)
    @Override
    public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
        if (this.level().isClientSide() || isWreck()) { super.vehicleShoot(living, uuid, targetPos); return; }
        GunData data = getGunData(getSeatIndex(living));
        if (data == null) { super.vehicleShoot(living, uuid, targetPos); return; }
        if (mortarFireTime != 0) return;                 // already mid fire sequence
        if (!data.canShoot(getAmmoSupplier())) return;

        mortarFireTime = MORTAR_FIRE_DELAY;
        mortarShooter = living;
        mortarUuid = uuid;
        mortarTargetPos = targetPos;

        var soundInfo = data.get(GunProp.SOUND_INFO);
        if (soundInfo != null && soundInfo.vehicleReload != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundInfo.vehicleReload, SoundSource.PLAYERS, 1f, 1f);
        }
        playShootSound3p(living, getSeatIndex(living));
    }

    private void launchMortarRound() {
        if (!(this.level() instanceof ServerLevel server) || mortarShooter == null) { mortarShooter = null; return; }
        int seatIndex = getSeatIndex(mortarShooter);
        GunData data = getGunData(seatIndex);
        if (data != null) {
            Vec3 dir = getShootVec(mortarShooter, 1f);
            Vec3 pos = getShootPos(mortarShooter, 1f);
            if (dir != null && pos != null) {
                modifyGunData(seatIndex, d -> {
                    if (d.canShoot(getAmmoSupplier())) {
                        d.shoot(new ShootParameters(getAmmoSupplier(), mortarShooter, server, pos, dir, d,
                                d.get(GunProp.SPREAD), true, mortarUuid, mortarTargetPos));
                    }
                });
                afterShoot(getGunData(seatIndex), dir);
            }
        }
        mortarShooter = null; mortarUuid = null; mortarTargetPos = null;
    }

    @Override public void baseTick() {
        super.baseTick();

        if (mortarFireTime > 0) {
            mortarFireTime--;
            if (mortarFireTime == 0) launchMortarRound();
        }

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