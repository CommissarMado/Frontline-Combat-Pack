package frontline.combat.fcp.entity.vehicle.Bradley;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.AtgmLauncherVehicle;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * M2 Bradley family (m2a2, m2a2_busk, m2a3, m2a3_busk) and the M6A2 Linebacker (m6a2: four Stingers in place of the
 * TOW) on one class; the registry name picks the geo and skins.
 * <p>
 * The TOW launcher on the turret's left side swings up while the gunner has the TOW selected and down while
 * it reloads (AtgmLauncherAnimation); the missiles fire from the raised tubes. The A3s add the Commander's
 * Independent Viewer on SBW's passenger weapon station.
 */
public class BradleyEntity extends CamoVehicleBase implements AtgmLauncherVehicle {

    public static final String TOW_WEAPON = "TOW";
    public static final String STINGER_WEAPON = "Stinger";

    /** Server-side TOW reload state, synced so every client lowers the launcher during it. */
    private static final EntityDataAccessor<Boolean> TOW_RELOADING =
            SynchedEntityData.defineId(BradleyEntity.class, EntityDataSerializers.BOOLEAN);

    public int INVENTORY_SIZE = 18;

    public BradleyEntity(EntityType<BradleyEntity> type, Level world) {
        super(type, world);
    }

    /** Registry path, e.g. "m2a3_busk". */
    public String variant() {
        return EntityType.getKey(getType()).getPath();
    }

    /** The M6A2: its launcher is the Stinger pod. */
    public boolean isLinebacker() {
        return variant().equals("m6a2");
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TOW_RELOADING, false);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level().isClientSide) {
            GunData tow = getGunData(atgmWeapon());
            this.entityData.set(TOW_RELOADING, tow != null && tow.reloading());
        }
    }

    @Override
    public boolean hasAtgmLauncher() {
        return true;
    }

    @Override
    public String atgmWeapon() {
        return isLinebacker() ? STINGER_WEAPON : TOW_WEAPON;
    }

    @Override
    public boolean atgmReloading() {
        return this.entityData.get(TOW_RELOADING);
    }

    /** True once the TOW has {@code count} or fewer missiles left (used to hide fired missiles). */
    public boolean towAtMost(int count) {
        return getAmmoCount(TOW_WEAPON) <= count;
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
}
