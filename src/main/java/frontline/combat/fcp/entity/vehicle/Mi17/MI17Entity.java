package frontline.combat.fcp.entity.vehicle.Mi17;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class MI17Entity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Boolean> TOGGLE6 = SynchedEntityData.defineId(MI17Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE4 = SynchedEntityData.defineId(MI17Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE5 = SynchedEntityData.defineId(MI17Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE2 = SynchedEntityData.defineId(MI17Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE1 = SynchedEntityData.defineId(MI17Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE3 = SynchedEntityData.defineId(MI17Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE = SynchedEntityData.defineId(MI17Entity.class, EntityDataSerializers.BOOLEAN);

    private boolean toggleInit = false;

    public MI17Entity(EntityType<MI17Entity> type, Level world) {
        super(type, world);

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TOGGLE6, true);
        this.entityData.define(TOGGLE4, true);
        this.entityData.define(TOGGLE5, true);
        this.entityData.define(TOGGLE2, true);
        this.entityData.define(TOGGLE1, true);
        this.entityData.define(TOGGLE3, true);
        this.entityData.define(TOGGLE, true);
    }

    public boolean hasToggle6() {return this.entityData.get(TOGGLE6);}
    public void setToggle6(boolean v) {this.entityData.set(TOGGLE6, v);}
    public void toggleToggle6() {setToggle6(!hasToggle6());}
    public boolean hasToggle4() {return this.entityData.get(TOGGLE4);}
    public void setToggle4(boolean v) {this.entityData.set(TOGGLE4, v);}
    public void toggleToggle4() {setToggle4(!hasToggle4());}
    public boolean hasToggle5() {return this.entityData.get(TOGGLE5);}
    public void setToggle5(boolean v) {this.entityData.set(TOGGLE5, v);}
    public void toggleToggle5() {setToggle5(!hasToggle5());}
    public boolean hasToggle2() {return this.entityData.get(TOGGLE2);}
    public void setToggle2(boolean v) {this.entityData.set(TOGGLE2, v);}
    public void toggleToggle2() {setToggle2(!hasToggle2());}
    public boolean hasToggle1() {return this.entityData.get(TOGGLE1);}
    public void setToggle1(boolean v) {this.entityData.set(TOGGLE1, v);}
    public void toggleToggle1() {setToggle1(!hasToggle1());}
    public boolean hasToggle3() {return this.entityData.get(TOGGLE3);}
    public void setToggle3(boolean v) {this.entityData.set(TOGGLE3, v);}
    public void toggleToggle3() {setToggle3(!hasToggle3());}
    public boolean hasToggle() {return this.entityData.get(TOGGLE);}
    public void setToggle(boolean v) {this.entityData.set(TOGGLE, v);}
    public void toggleToggle() {setToggle(!hasToggle());}

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Toggle6", hasToggle6());
        compound.putBoolean("Toggle4", hasToggle4());
        compound.putBoolean("Toggle5", hasToggle5());
        compound.putBoolean("Toggle2", hasToggle2());
        compound.putBoolean("Toggle1", hasToggle1());
        compound.putBoolean("Toggle3", hasToggle3());
        compound.putBoolean("Toggle", hasToggle());
        compound.putBoolean("ToggleInit", toggleInit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ToggleInit")) toggleInit = compound.getBoolean("ToggleInit");
        if (compound.contains("Toggle6")) setToggle6(compound.getBoolean("Toggle6"));
        if (compound.contains("Toggle4")) setToggle4(compound.getBoolean("Toggle4"));
        if (compound.contains("Toggle5")) setToggle5(compound.getBoolean("Toggle5"));
        if (compound.contains("Toggle2")) setToggle2(compound.getBoolean("Toggle2"));
        if (compound.contains("Toggle1")) setToggle1(compound.getBoolean("Toggle1"));
        if (compound.contains("Toggle3")) setToggle3(compound.getBoolean("Toggle3"));
        if (compound.contains("Toggle")) setToggle(compound.getBoolean("Toggle"));
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // Randomise each toggle independently, once on first spawn (like the OH-1's stub-wing
        // pods / the UAZ-3303's tent), then persist whatever was rolled (or later toggled).
        if (!this.level().isClientSide() && !toggleInit) {
            setToggle6(this.random.nextBoolean());
            setToggle4(this.random.nextBoolean());
            setToggle5(this.random.nextBoolean());
            setToggle2(this.random.nextBoolean());
            setToggle1(this.random.nextBoolean());
            setToggle3(this.random.nextBoolean());
            setToggle(this.random.nextBoolean());
            toggleInit = true;
        }

    }
}
