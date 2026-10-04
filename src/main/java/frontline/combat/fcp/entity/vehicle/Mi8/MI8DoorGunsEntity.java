package frontline.combat.fcp.entity.vehicle.Mi8;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector4d;
public class MI8DoorGunsEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Boolean> TOGGLE6 = SynchedEntityData.defineId(MI8DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE4 = SynchedEntityData.defineId(MI8DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE5 = SynchedEntityData.defineId(MI8DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE2 = SynchedEntityData.defineId(MI8DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE1 = SynchedEntityData.defineId(MI8DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE3 = SynchedEntityData.defineId(MI8DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE = SynchedEntityData.defineId(MI8DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE7 = SynchedEntityData.defineId(MI8DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean toggleInit = false;

    // Single side door gun (PKM) - same hand-driven yaw/pitch architecture as
    // MI17DoorGunsEntity (see that class and Uh60MinigunEntity for the full rationale), just
    // with one gun instead of two. The copilot-controlled nose gun (PKT) is a completely separate
    // system: it needs no hand-driven aiming Java at all, since it's wired to SBW's own native
    // single-turret system (TurretPos/BarrelPos/TurretControllerIndex in mi8_door_guns.json) -
    // see MI8DoorGunsModel's "barrel2" case for the (equally Java-light) visual bone wiring that
    // mirrors it, driving the nose gun's own pre-existing, already-modeled "barrel2" bone rather
    // than a hand-made stand-in.
    // Right/Left describe the physical side of the fuselage as rendered in-game, not the raw
    // geo's X sign in isolation - this model's positive-X side is labeled the aircraft's LEFT
    // here (matching the same label applied to MI17DoorGunsEntity's identical mount/barrel
    // bones); only one gun exists on this vehicle so this labeling is cosmetic/doc-comment only,
    // it doesn't affect gameplay.
    // Seat index: the gunner's Seats[] entry lives at index 2 (0-indexed) in mi8_door_guns.json -
    // i.e. "seat 3" if you count 1-indexed/including the pilot as seat 1. An earlier round swapped
    // the wrong pair of seats (index 3 <-> 4, moving the gun to index 4 = "seat 5") - this is the
    // corrected swap (index 2 <-> 3). IMPORTANT: this constant is duplicated as a literal seat
    // number in MI8DoorGunsModel's "barrel" case (vehicle.getGunYawDeg(2, ...) /
    // getGunPitchDeg(2, ...)) since Model.java is a different class and can't reference this
    // private constant directly - keep both in sync with whichever Seats[] index actually carries
    // Weapons:["PKMLeft"], or the gun will silently stop responding to input (always reads yaw/
    // pitch as 0 from an empty seat) without any error, which is exactly what happened last round.
    private static final int LEFT_GUN_SEAT = 2;

    private static final Vec3 LEFT_TURRET_POS = new Vec3(1.265, 1.52226, 1.6925);
    private static final Vec3 LEFT_BARREL_POS = new Vec3(0.11, 0.015, -0.155);

    public MI8DoorGunsEntity(EntityType<MI8DoorGunsEntity> type, Level world) {
        super(type, world);
        getPositionTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_POS));
        getVectorTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelVector(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_POS));
        getPositionTransform().put("LeftTurret",
                (Float pt) -> getGunTurretTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS));

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
        this.entityData.define(TOGGLE7, true);
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
    public boolean hasToggle7() {return this.entityData.get(TOGGLE7);}
    public void setToggle7(boolean v) {this.entityData.set(TOGGLE7, v);}
    public void toggleToggle7() {setToggle7(!hasToggle7());}

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
        compound.putBoolean("Toggle7", hasToggle7());
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
        if (compound.contains("Toggle7")) setToggle7(compound.getBoolean("Toggle7"));
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
            setToggle7(this.random.nextBoolean());
            toggleInit = true;
        }

    }

    public float getGunYawDeg(int seatIndex, float partialTicks) {
        if (getNthEntity(seatIndex) == null) return 0f;
        Vec3 targetVec = getShootVec(seatIndex, partialTicks);
        Vec3 defaultVec = getDefaultBarrelDirection(seatIndex, partialTicks);
        if (targetVec == null || defaultVec == null) return 0f;
        double diffY = Mth.wrapDegrees(-VehicleVecUtils.getYRotFromVector(targetVec) + VehicleVecUtils.getYRotFromVector(defaultVec));
        return (float) -diffY;
    }

    public float getGunPitchDeg(int seatIndex, float partialTicks) {
        if (getNthEntity(seatIndex) == null) return 0f;
        Vec3 targetVec = getShootVec(seatIndex, partialTicks);
        Vec3 defaultVec = getDefaultBarrelDirection(seatIndex, partialTicks);
        if (targetVec == null || defaultVec == null) return 0f;
        double diffX = Mth.wrapDegrees(-VehicleVecUtils.getXRotFromVector(targetVec) + VehicleVecUtils.getXRotFromVector(defaultVec));
        return (float) -diffX;
    }

    private Matrix4d getGunBarrelTransform(float partialTicks, int seatIndex, Vec3 turretPos, Vec3 barrelPos) {
        Matrix4d transform = getVehicleTransformWithCustomPitch(partialTicks);
        // Same fix as MI17DoorGunsEntity: turretPos/barrelPos are NOT two independent pivots here
        // (no separate yaw-ring/pitch-trunnion bones) - both yaw and pitch are applied to the SAME
        // single "barrel" bone in MI8DoorGunsModel, which always rotates around that one bone's
        // own fixed pivot (turretPos+barrelPos - the absolute point barrelPos was computed
        // relative to). translate(turretPos) -> rotate(yaw) -> translate(barrelPos) -> rotate(pitch)
        // treats barrelPos as a second pivot that orbits around turretPos as yaw changes, which
        // the real single-bone model never does; see MI17DoorGunsEntity.getGunBarrelTransform for
        // the full writeup (this file shares the identical door-gun geometry/rig and had the exact
        // same latent desync bug, just not separately reported for this vehicle).
        Vec3 pivot = turretPos.add(barrelPos);
        transform.translate(pivot.x, pivot.y, pivot.z);
        transform.rotate(Axis.YP.rotationDegrees(getGunYawDeg(seatIndex, partialTicks)));
        // Same fix as MI17DoorGunsEntity.getGunBarrelTransform: this gun's rest direction is local
        // +X, not +Z (see that file's comment for the full derivation), so pitch must rotate around
        // Z, not X - rotating around X leaves the dominant ~0.57-unit X component of
        // ShootPos.Positions untouched under pitch and only sweeps the ~0.03-unit Y/Z remainder,
        // which is the "barely elevates" bug.
        // Sign: this vehicle's one door gun is the same LEFT-side gun/geometry as
        // MI17DoorGunsEntity's LeftBarrel, which needed its shootpos pitch flipped relative to the
        // original -getGunPitchDeg baseline (confirmed in-game: shootpos was inverted while this
        // gun's own visual bone was already elevating correctly and was left alone). Dropping the
        // leading "-" here applies that same one-line flip.
        transform.rotate(Axis.ZP.rotationDegrees(getGunPitchDeg(seatIndex, partialTicks)));
        return transform;
    }

    private Matrix4d getGunTurretTransform(float partialTicks, int seatIndex, Vec3 turretPos) {
        Matrix4d transform = getVehicleTransformWithCustomPitch(partialTicks);
        transform.translate(turretPos.x, turretPos.y, turretPos.z);
        transform.rotate(Axis.YP.rotationDegrees(getGunYawDeg(seatIndex, partialTicks)));
        return transform;
    }

    private Vec3 getGunBarrelVector(float partialTicks, int seatIndex, Vec3 turretPos, Vec3 barrelPos) {
        Matrix4d transform = getGunBarrelTransform(partialTicks, seatIndex, turretPos, barrelPos);
        Vector4d root = transform.transform(new Vector4d(0, 0, 0, 1));
        Vector4d target = transform.transform(new Vector4d(0, 0, 1, 1));
        return new Vec3(root.x, root.y, root.z).vectorTo(new Vec3(target.x, target.y, target.z));
    }
}
