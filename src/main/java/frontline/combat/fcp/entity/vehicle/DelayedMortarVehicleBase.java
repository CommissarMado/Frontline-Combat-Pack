package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public abstract class DelayedMortarVehicleBase extends IndirectFireVehicleBase {

    private static final int MORTAR_FIRE_DELAY = 20;

    private int mortarFireTime;
    private LivingEntity mortarShooter;
    private UUID mortarUuid;
    private Vec3 mortarTargetPos;

    protected DelayedMortarVehicleBase(EntityType<? extends VehicleEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void vehicleShoot(LivingEntity shooter, UUID uuid, Vec3 targetPos) {
        if (this.level().isClientSide() || isWreck()) {
            super.vehicleShoot(shooter, uuid, targetPos);
            return;
        }

        GunData data = getGunData(getSeatIndex(shooter));
        if (data == null || mortarFireTime != 0 || !data.canShoot(getAmmoSupplier())) {
            return;
        }
        if (!canStartIndirectShot(shooter)) {
            return;
        }

        mortarFireTime = MORTAR_FIRE_DELAY;
        mortarShooter = shooter;
        mortarUuid = uuid;
        mortarTargetPos = targetPos;

        var soundInfo = data.get(GunProp.SOUND_INFO);
        if (soundInfo != null && soundInfo.vehicleReload != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    soundInfo.vehicleReload, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        playShootSound3p(shooter, getSeatIndex(shooter));
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (mortarFireTime > 0 && --mortarFireTime == 0) {
            launchMortarRound();
        }
    }

    private void launchMortarRound() {
        if (!(this.level() instanceof ServerLevel server) || mortarShooter == null) {
            clearQueuedShot();
            return;
        }

        if (isFireControlActive()) {
            fireIndirectRound(mortarShooter, mortarUuid, false);
            clearQueuedShot();
            return;
        }

        int seatIndex = getSeatIndex(mortarShooter);
        GunData data = getGunData(seatIndex);
        if (data != null) {
            Vec3 direction = getShootVec(mortarShooter, 1.0f);
            Vec3 position = getShootPos(mortarShooter, 1.0f);
            if (direction != null && position != null) {
                modifyGunData(seatIndex, current -> {
                    if (current.canShoot(getAmmoSupplier())) {
                        current.shoot(new ShootParameters(
                                getAmmoSupplier(), mortarShooter, server, position, direction, current,
                                current.get(GunProp.SPREAD), true, mortarUuid, mortarTargetPos
                        ));
                    }
                });
                afterShoot(getGunData(seatIndex), direction);
            }
        }
        clearQueuedShot();
    }

    private void clearQueuedShot() {
        mortarShooter = null;
        mortarUuid = null;
        mortarTargetPos = null;
    }
}
