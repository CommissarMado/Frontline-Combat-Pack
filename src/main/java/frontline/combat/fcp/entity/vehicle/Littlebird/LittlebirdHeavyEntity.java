package frontline.combat.fcp.entity.vehicle.Littlebird;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * "AH-6M Littlebird" - the heavy-attack Littlebird variant. Very similar to the normal
 * littlebird_armed, but the right-hand minigun/rocket pod pair has been replaced with a fixed
 * "roketa" rack carrying two wire-guided Hellfire missiles (HELFIRE/HELFIRE2), fired by the
 * co-pilot through the new camera turret/barrel bones - the same "Hellfire2" pattern the
 * Blackhawk (MH-60L) uses for its co-pilot-fired missiles, and the same native
 * turret/barrel bone pair the Venom uses for its front sensor.
 */
public class LittlebirdHeavyEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private int previousCannonAmmo = -1;
    private float barrelRotation = 0f;
    private float barrelRotationOld = 0f;

    // "door toggle" is the shared parent bone of both "door" and "door2" in
    // littlebird_heavy.geo.json - hiding it hides both side doors at once, the same
    // toggle-cosmetic pattern as the UAZ-3303's tent.
    private static final EntityDataAccessor<Boolean> DOORS = SynchedEntityData.defineId(LittlebirdHeavyEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean doorsInit = false;

    public LittlebirdHeavyEntity(EntityType<LittlebirdHeavyEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DOORS, true);
    }

    public boolean hasDoors() {return this.entityData.get(DOORS);}
    public void setDoors(boolean v) {this.entityData.set(DOORS, v);}
    public void toggleDoors() {setDoors(!hasDoors());}

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Doors", hasDoors());
        compound.putBoolean("DoorsInit", doorsInit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("DoorsInit")) doorsInit = compound.getBoolean("DoorsInit");
        if (compound.contains("Doors")) setDoors(compound.getBoolean("Doors"));
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // Randomise the doors once on first spawn (like the UAZ-3303's tent), then persist it.
        if (!this.level().isClientSide() && !doorsInit) {
            setDoors(this.random.nextBoolean());
            doorsInit = true;
        }

        // Store previous barrel rotation for smooth interpolation
        barrelRotationOld = barrelRotation;

        // Check if cannon ammo has changed (meaning it was fired) - only one minigun barrel
        // survives on this variant (BarrelRotationController1), unlike littlebird_armed's two.
        int currentAmmo = getAmmoCount("Cannon");

        // Initialize on first tick
        if (previousCannonAmmo == -1) {
            previousCannonAmmo = currentAmmo;
        }

        // If ammo decreased, increment barrel rotation
        if (currentAmmo < previousCannonAmmo) {
            barrelRotation += 20f; // Increment by 20 degrees per shot
            if (barrelRotation >= 360f) {
                barrelRotation -= 360f; // Wrap around at 360 degrees
            }
        }

        // Update stored ammo count for next tick
        previousCannonAmmo = currentAmmo;
    }

    // Same "hide the missile once it's been fired" mechanism as the Blackhawk (Mh60lEntity):
    // each Hellfire2 bone (HELFIRE/HELFIRE2) is toggled off individually as the Hellfire2
    // ammo count counts down from Magazine-1 to 0. See LittlebirdHeavyModel for the bone wiring.
    public boolean GetWeaponState(String WeaponName, int Count) {
        if (getAmmoCount(WeaponName) == Count)
            return true;
        else if (getAmmoCount(WeaponName) < Count)
            return true;
        else
            return false;
    }

    public float getBarrelRot() {
        return barrelRotation;
    }

    public float getBarrelRot0() {
        return barrelRotationOld;
    }
}
