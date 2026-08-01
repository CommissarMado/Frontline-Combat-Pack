package frontline.combat.fcp.entity.vehicle.Emplacement;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModSounds;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Shared base for TOW-style emplacements (TOW, Kornet): reloaded like the base SBW TOW.
 * Firing starts an RPM-based reload cooldown but leaves the round shown. The dismounted gunner's
 * FIRST right-click clears the spent round (LOADED=false, Magazine bone hides); once the cooldown
 * has elapsed, right-clicking with the ammo item reloads (LOADED=true, Magazine bone reappears).
 * Unlike the base TOW there is NO cooldown countdown message and NO reload sound.
 */
public abstract class EmplacementEntity extends CamoVehicleBase {
    public static final EntityDataAccessor<Boolean> LOADED =
            SynchedEntityData.defineId(EmplacementEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> RELOAD_COOLDOWN =
            SynchedEntityData.defineId(EmplacementEntity.class, EntityDataSerializers.INT);

    public EmplacementEntity(EntityType<? extends VehicleEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LOADED, false);
        this.entityData.define(RELOAD_COOLDOWN, 0);
    }

    public boolean isLoaded() { return this.entityData.get(LOADED); }
    public void setLoaded(boolean v) { this.entityData.set(LOADED, v); }
    public int getReloadCooldown() { return this.entityData.get(RELOAD_COOLDOWN); }
    public void setReloadCooldown(int v) { this.entityData.set(RELOAD_COOLDOWN, v); }

    @Override
    public void addAdditionalSaveData(CompoundTag c) {
        super.addAdditionalSaveData(c);
        c.putBoolean("Loaded", isLoaded());
        c.putInt("ReloadCoolDown", getReloadCooldown());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag c) {
        super.readAdditionalSaveData(c);
        if (c.contains("Loaded")) setLoaded(c.getBoolean("Loaded"));
        if (c.contains("ReloadCoolDown")) setReloadCooldown(c.getInt("ReloadCoolDown"));
    }

    private int reloadCoolDownTicks() {
        int rpm = vehicleWeaponRpm(0);
        if (rpm <= 0) return 20;
        return (int) Math.ceil(20f / (rpm / 60f));
    }

    /** Emplacements are fixed installations — nothing can shove them. */
    @Override
    public boolean isPushable() { return false; }

    /** TOW-style get-out reload. Clamped MG/grenade turrets override this to false (normal inventory reload). */
    protected boolean needsManualReload() { return true; }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!needsManualReload()) {
            return super.interact(player, hand);
        }
        GunData gunData = getGunData(0);
        if (gunData != null) {
            ItemStack stack = player.getMainHandItem();
            if (gunData.hasEnoughAmmoToShoot(player)) {
                setLoaded(true);
                return super.interact(player, hand);
            }
            if (!isLoaded()) {
                // Multi-ammo aware: find the consumer whose ammo item matches what's held (any accepted type).
                java.util.List<com.atsuishio.superbwarfare.data.gun.AmmoConsumer> consumers =
                        gunData.get(com.atsuishio.superbwarfare.data.gun.GunProp.AMMO_CONSUMER);
                int matchIndex = -1;
                for (int i = 0; i < consumers.size(); i++) {
                    if (consumers.get(i).isAmmoItem(stack)) { matchIndex = i; break; }
                }
                if (matchIndex < 0) {
                    return super.interact(player, hand);
                }
                int coolDown = reloadCoolDownTicks();
                if (level() instanceof ServerLevel serverLevel && getReloadCooldown() == 0) {
                    final int idx = matchIndex;
                    modifyGunData(0, data -> { data.changeAmmoConsumer(idx, player); data.reloadAmmo(player, false); });
                    setLoaded(true);
                    serverLevel.playSound(null, blockPosition(), ModSounds.TYPE_63_RELOAD.get(),
                            SoundSource.PLAYERS, 1f, this.random.nextFloat() * 0.1f + 0.9f);
                } else {
                    player.displayClientMessage(Component.literal(String.format("%.1f / %.1f",
                            (coolDown - getReloadCooldown()) / 20f, coolDown / 20f)), true);
                }
            } else {
                // First right-click after firing clears the spent round.
                setLoaded(false);
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        int cd = getReloadCooldown();
        if (cd > 0 && !this.level().isClientSide()) setReloadCooldown(cd - 1);
    }

    @Override
    public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
        super.vehicleShoot(living, uuid, targetPos);
        // Start the reload cooldown; keep the round shown until the first post-fire right-click.
        setReloadCooldown(reloadCoolDownTicks());
    }
}
