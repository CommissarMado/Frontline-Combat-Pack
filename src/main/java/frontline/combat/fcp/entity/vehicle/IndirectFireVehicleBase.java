package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.item.misc.FiringParametersItem;
import com.atsuishio.superbwarfare.item.misc.FiringParametersItemKt;
import frontline.combat.fcp.firecontrol.FireControlComputation;
import frontline.combat.fcp.firecontrol.FireControlSolution;
import frontline.combat.fcp.firecontrol.FireControlStatus;
import frontline.combat.fcp.firecontrol.IndirectFireBallistics;
import frontline.combat.fcp.firecontrol.TrajectoryMode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public abstract class IndirectFireVehicleBase extends CamoVehicleBase implements IndirectFireVehicle {

    private static final EntityDataAccessor<Boolean> FIRE_CONTROL_ACTIVE =
            SynchedEntityData.defineId(IndirectFireVehicleBase.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> FIRE_CONTROL_TARGET =
            SynchedEntityData.defineId(IndirectFireVehicleBase.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> FIRE_CONTROL_RADIUS =
            SynchedEntityData.defineId(IndirectFireVehicleBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FIRE_CONTROL_TRAJECTORY =
            SynchedEntityData.defineId(IndirectFireVehicleBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FIRE_CONTROL_STATUS =
            SynchedEntityData.defineId(IndirectFireVehicleBase.class, EntityDataSerializers.INT);

    private static final double MOVEMENT_THRESHOLD = 0.05;
    private static final int STABILIZATION_TICKS = 10;
    private static final double READY_TOLERANCE_DEGREES = 1.0;
    private static final int MAX_SAMPLE_ATTEMPTS = 16;

    private int stationaryTicks;
    private int lastBlockedMessageTick = -1000;

    protected IndirectFireVehicleBase(EntityType<? extends VehicleEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FIRE_CONTROL_ACTIVE, false);
        this.entityData.define(FIRE_CONTROL_TARGET, BlockPos.ZERO);
        this.entityData.define(FIRE_CONTROL_RADIUS, 0);
        this.entityData.define(FIRE_CONTROL_TRAJECTORY, TrajectoryMode.LOW.ordinal());
        this.entityData.define(FIRE_CONTROL_STATUS, FireControlStatus.INACTIVE.ordinal());
    }

    @Override
    public boolean isFireControlActive() {
        return this.entityData.get(FIRE_CONTROL_ACTIVE);
    }

    @Override
    public BlockPos getFireControlTarget() {
        return this.entityData.get(FIRE_CONTROL_TARGET);
    }

    @Override
    public int getFireControlRadius() {
        return this.entityData.get(FIRE_CONTROL_RADIUS);
    }

    @Override
    public TrajectoryMode getFireControlTrajectory() {
        return TrajectoryMode.fromId(this.entityData.get(FIRE_CONTROL_TRAJECTORY));
    }

    @Override
    public FireControlStatus getFireControlStatus() {
        return FireControlStatus.fromId(this.entityData.get(FIRE_CONTROL_STATUS));
    }

    protected void setFireControlStatus(FireControlStatus status) {
        if (!this.level().isClientSide) {
            this.entityData.set(FIRE_CONTROL_STATUS, status.ordinal());
        }
    }

    @Override
    public FireControlComputation getFireControlComputation() {
        return IndirectFireBallistics.solve(
                this,
                getTurretControllerIndex(),
                getFireControlTarget(),
                getFireControlTrajectory()
        );
    }

    @Override
    public boolean applyFireControl(BlockPos target, int radius, TrajectoryMode mode, Entity actor) {
        if (this.level().isClientSide) {
            return false;
        }
        if (isWreck()) {
            notifyActor(actor, FireControlStatus.WRECKED.translationKey());
            return false;
        }
        if (radius < 0 || radius > IndirectFireBallistics.MAX_RADIUS
                || target.getY() < this.level().getMinBuildHeight()
                || target.getY() >= this.level().getMaxBuildHeight()
                || !this.level().getWorldBorder().isWithinBounds(target)) {
            notifyActor(actor, "message.fcp.fire_control.invalid_input");
            return false;
        }

        FireControlComputation computation = IndirectFireBallistics.solve(
                this, getTurretControllerIndex(), target, mode
        );
        if (!computation.isSuccess()) {
            notifyActor(actor, computation.status().translationKey());
            return false;
        }

        this.entityData.set(FIRE_CONTROL_TARGET, target.immutable());
        this.entityData.set(FIRE_CONTROL_RADIUS, radius);
        this.entityData.set(FIRE_CONTROL_TRAJECTORY, mode.ordinal());
        this.entityData.set(FIRE_CONTROL_ACTIVE, true);
        stationaryTicks = 0;
        setFireControlStatus(isMovingForFireControl()
                ? FireControlStatus.MOVING
                : FireControlStatus.ALIGNING);
        if (actor instanceof Player player) {
            player.displayClientMessage(Component.translatable(
                    "message.fcp.fire_control.applied",
                    target.getX(), target.getY(), target.getZ(), radius,
                    Component.translatable(mode.translationKey())
            ), true);
        }
        return true;
    }

    @Override
    public void clearFireControl(Entity actor) {
        if (this.level().isClientSide) {
            return;
        }
        this.entityData.set(FIRE_CONTROL_ACTIVE, false);
        this.entityData.set(FIRE_CONTROL_STATUS, FireControlStatus.INACTIVE.ordinal());
        stationaryTicks = 0;
        notifyActor(actor, "message.fcp.fire_control.cleared");
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()
                && stack.is(com.atsuishio.superbwarfare.init.ModItems.FIRING_PARAMETERS.get())) {
            if (!this.level().isClientSide) {
                FiringParametersItem.Parameters parameters = FiringParametersItemKt.getFiringParameters(stack);
                applyFireControl(
                        parameters.pos(),
                        parameters.radius(),
                        TrajectoryMode.fromFiringParameters(parameters.isDepressed()),
                        player
                );
            }
            player.swing(hand);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.interact(player, hand);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        tickFireControl();
    }

    private void tickFireControl() {
        if (!isFireControlActive()) {
            stationaryTicks = 0;
            setFireControlStatus(FireControlStatus.INACTIVE);
            return;
        }
        if (isWreck()) {
            stationaryTicks = 0;
            setFireControlStatus(FireControlStatus.WRECKED);
            return;
        }
        if (isMovingForFireControl()) {
            stationaryTicks = 0;
            setFireControlStatus(FireControlStatus.MOVING);
            return;
        }
        if (stationaryTicks < STABILIZATION_TICKS) {
            stationaryTicks++;
            setFireControlStatus(FireControlStatus.MOVING);
            return;
        }

        FireControlComputation computation = getFireControlComputation();
        if (!computation.isSuccess()) {
            setFireControlStatus(computation.status());
            return;
        }

        // Drop residual mouse look input so SBW systems cannot re-steer the turret.
        setMouseMoveSpeedX(0.0f);
        setMouseMoveSpeedY(0.0f);

        FireControlSolution solution = computation.solution();
        Vec3 desired = solution.direction();
        turretAutoAimFromVector(desired);

        // Measure against the barrel vector — the same basis SBW's turretAutoAimFromVector uses.
        // getShootVec(seat) needs a living passenger and can diverge from the barrel transform.
        Vec3 actual = getBarrelVector(1.0f);
        if (actual == null || actual.lengthSqr() < 1.0E-8) {
            actual = getShootVec(getTurretControllerIndex(), 1.0f);
        }
        if (actual == null || actual.lengthSqr() < 1.0E-8 || desired.lengthSqr() < 1.0E-8) {
            setFireControlStatus(FireControlStatus.ALIGNING);
            return;
        }
        double dot = Mth.clamp(actual.normalize().dot(desired.normalize()), -1.0, 1.0);
        double error = Math.toDegrees(Math.acos(dot));
        setFireControlStatus(error <= READY_TOLERANCE_DEGREES
                ? FireControlStatus.READY
                : FireControlStatus.ALIGNING);
    }

    protected boolean canStartIndirectShot(LivingEntity shooter) {
        if (!isFireControlActive()) {
            return true;
        }
        if (getFireControlStatus() != FireControlStatus.READY || isMovingForFireControl()) {
            notifyBlocked(shooter, getFireControlStatus());
            return false;
        }
        return true;
    }

    protected boolean fireIndirectRound(
            LivingEntity shooter,
            UUID targetEntityUuid,
            boolean playShootSound
    ) {
        if (!(this.level() instanceof ServerLevel server)
                || !isFireControlActive()
                || !canStartIndirectShot(shooter)) {
            return false;
        }

        int seatIndex = getSeatIndex(shooter);
        GunData data = getGunData(seatIndex);
        if (seatIndex < 0 || data == null || !data.canShoot(getAmmoSupplier())) {
            return false;
        }

        FireControlComputation computation = null;
        Vec3 sampledTarget = null;
        for (int i = 0; i < MAX_SAMPLE_ATTEMPTS; i++) {
            Vec3 candidate = IndirectFireBallistics.sampleTarget(
                    getFireControlTarget(), getFireControlRadius(), getRandom()
            );
            FireControlComputation candidateComputation = IndirectFireBallistics.solve(
                    this, seatIndex, candidate, getFireControlTrajectory()
            );
            if (candidateComputation.isSuccess()) {
                sampledTarget = candidate;
                computation = candidateComputation;
                break;
            }
        }

        if (computation == null) {
            sampledTarget = getFireControlTarget().getCenter();
            computation = IndirectFireBallistics.solve(
                    this, seatIndex, sampledTarget, getFireControlTrajectory()
            );
        }
        if (!computation.isSuccess()) {
            setFireControlStatus(computation.status());
            notifyBlocked(shooter, computation.status());
            return false;
        }

        Vec3 direction = computation.solution().direction();
        Vec3 shootPosition = getShootPos(shooter, 1.0f);
        boolean[] fired = {false};
        Vec3 finalSampledTarget = sampledTarget;
        modifyGunData(seatIndex, current -> {
            if (current.canShoot(getAmmoSupplier())) {
                current.shoot(new ShootParameters(
                        getAmmoSupplier(), shooter, server, shootPosition, direction, current,
                        0.0, true, targetEntityUuid, finalSampledTarget
                ));
                fired[0] = true;
            }
        });

        if (fired[0]) {
            afterShoot(getGunData(seatIndex), direction);
            if (playShootSound) {
                playShootSound3p(shooter, seatIndex);
            }
        }
        return fired[0];
    }

    @Override
    public void vehicleShoot(LivingEntity shooter, UUID targetEntityUuid, Vec3 targetPos) {
        if (!this.level().isClientSide && isFireControlActive()) {
            fireIndirectRound(shooter, targetEntityUuid, true);
            return;
        }
        super.vehicleShoot(shooter, targetEntityUuid, targetPos);
    }

    protected boolean isMovingForFireControl() {
        return this.getDeltaMovement().horizontalDistance() > MOVEMENT_THRESHOLD;
    }

    private void notifyBlocked(LivingEntity actor, FireControlStatus status) {
        if (!(actor instanceof Player player) || this.tickCount - lastBlockedMessageTick < 20) {
            return;
        }
        lastBlockedMessageTick = this.tickCount;
        player.displayClientMessage(Component.translatable(
                "message.fcp.fire_control.blocked",
                Component.translatable(status.translationKey())
        ), true);
    }

    private static void notifyActor(Entity actor, String translationKey) {
        if (actor instanceof Player player) {
            player.displayClientMessage(Component.translatable(translationKey), true);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("FCPFireControlActive", isFireControlActive());
        tag.putLong("FCPFireControlTarget", getFireControlTarget().asLong());
        tag.putInt("FCPFireControlRadius", getFireControlRadius());
        tag.putInt("FCPFireControlTrajectory", getFireControlTrajectory().ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (!tag.contains("FCPFireControlActive")) {
            return;
        }
        this.entityData.set(FIRE_CONTROL_ACTIVE, tag.getBoolean("FCPFireControlActive"));
        this.entityData.set(FIRE_CONTROL_TARGET, BlockPos.of(tag.getLong("FCPFireControlTarget")));
        this.entityData.set(FIRE_CONTROL_RADIUS,
                Mth.clamp(tag.getInt("FCPFireControlRadius"), 0, IndirectFireBallistics.MAX_RADIUS));
        this.entityData.set(FIRE_CONTROL_TRAJECTORY,
                TrajectoryMode.fromId(tag.getInt("FCPFireControlTrajectory")).ordinal());
        this.entityData.set(FIRE_CONTROL_STATUS, isFireControlActive()
                ? FireControlStatus.MOVING.ordinal()
                : FireControlStatus.INACTIVE.ordinal());
    }
}
