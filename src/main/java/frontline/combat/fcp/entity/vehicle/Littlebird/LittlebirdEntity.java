package frontline.combat.fcp.entity.vehicle.Littlebird;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class LittlebirdEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    // "door toggle" is the shared parent bone of both "door" and "door2" in littlebird.geo.json
    // (pushed alongside the new camera bones) - hiding it hides both side doors at once, the same
    // toggle-cosmetic pattern as the UAZ-3303's tent, just with a single bone covering both sides
    // instead of needing OH-1's two independent toggles.
    private static final EntityDataAccessor<Boolean> DOORS = SynchedEntityData.defineId(LittlebirdEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean doorsInit = false;

    public LittlebirdEntity(EntityType<LittlebirdEntity> type, Level world) {
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
    }
}
