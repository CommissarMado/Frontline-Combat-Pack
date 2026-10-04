package frontline.combat.fcp.entity.vehicle.Mi17;

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
public class MI17DoorGunsEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Boolean> TOGGLE6 = SynchedEntityData.defineId(MI17DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE4 = SynchedEntityData.defineId(MI17DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE5 = SynchedEntityData.defineId(MI17DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE2 = SynchedEntityData.defineId(MI17DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE1 = SynchedEntityData.defineId(MI17DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE3 = SynchedEntityData.defineId(MI17DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE = SynchedEntityData.defineId(MI17DoorGunsEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean toggleInit = false;

    // Two fully independent door guns (PKMs), each mounted on its own seat and aimed by hand -
    // same architecture as Uh60MinigunEntity (see that class for the full rationale: neither the
    // vehicle's built-in Turret nor PassengerWeaponStation system supports two independent guns,
    // and BoundBones only applies to the newer non-GeckoLib rendering pipeline this vehicle
    // doesn't use). Unlike the Uh-60's miniguns, these two guns rest facing OUTWARD (sideways,
    // away from the fuselage) rather than forward - that's purely a matter of what
    // "DefaultBarrelDirection" each weapon's ShootPos declares in mi17_door_guns.json ([1,0,0] /
    // [-1,0,0]); the yaw/pitch math below only ever measures the OFFSET between the seat's
    // current aim and that configured rest direction, so it works the same regardless of which
    // way "rest" points. "door gun"/"door_gun2" are the pintle MOUNTS and are never rotated at
    // all - both yaw and pitch are applied to their "barrel"/"barrel2" children instead (whose
    // own body2+magazin / body3+magazin2 children are the actual PKM), so only the gun itself
    // moves. See MI17DoorGunsModel for the bone wiring and the baked-rotation compose details.
    // Right/Left here describe which physical side of the fuselage each gun is actually on as
    // rendered in-game, NOT the raw geo's X sign in isolation. NOTE: an earlier round renamed
    // RIGHT_GUN_SEAT/LEFT_GUN_SEAT (and their TURRET_POS/BARREL_POS constants) but kept the same
    // seat-index -> bone binding underneath, so that rename had zero effect on which physical
    // gun each seat actually controls - it only changed what the constants were called. Reported
    // as still mixed up afterward, which is consistent with that: nothing about the actual
    // seat-to-bone pairing has been changed yet. This round leaves LEFT_GUN_SEAT=3 (-> "barrel",
    // the +X-pivot bone) and RIGHT_GUN_SEAT=2 (-> "barrel2", the -X-pivot bone) as they already
    // were, rather than guessing at a swap with no new evidence - the "spin uncontrollably" bug
    // fixed this same round was very likely making left/right hard to judge in the first place
    // (a gun that's spinning out of control will visually cross to the wrong side on its own).
    // Re-test after that fix; if left/right is still backwards, swap LEFT_GUN_SEAT/RIGHT_GUN_SEAT
    // and their TURRET_POS/BARREL_POS pairs with the corresponding seat index used in
    // MI17DoorGunsModel's "barrel"/"barrel2" cases (both must move together, or the visual gun
    // and its bullet-spawn position will point at two different physical guns).
    private static final int LEFT_GUN_SEAT = 3;
    private static final int RIGHT_GUN_SEAT = 2;

    private static final Vec3 LEFT_TURRET_POS = new Vec3(1.265, 1.52226, 1.6925);
    private static final Vec3 LEFT_BARREL_POS = new Vec3(0.11, 0.015, -0.155);
    private static final Vec3 RIGHT_TURRET_POS = new Vec3(-1.265, 1.52226, 1.6925);
    private static final Vec3 RIGHT_BARREL_POS = new Vec3(-0.11, 0.015, -0.145);

    public MI17DoorGunsEntity(EntityType<MI17DoorGunsEntity> type, Level world) {
        super(type, world);
        // positionTransform/vectorTransform are populated by the superclass's own
        // registerTransforms() during super(type, world), so it's safe to add our own entries
        // right after - same pattern as Uh60MinigunEntity.
        // pitchSign: an earlier round tried negating RIGHT's gameplay pitch to mirror the visual
        // negation in MI17DoorGunsModel's "barrel2" case (bone.setRotX(-getGunPitchDeg(...))),
        // reasoning that a mirrored bone needed a mirrored gameplay sign too. Confirmed WRONG
        // in-game at the time ("the shootpos pitch is now inverted") and reverted to 1f for both
        // sides. Since then, with the shootpos and the visual bone judged independently, it turned
        // out the LEFT gun's shootpos specifically needed that flip after all (its own visual bone
        // was already elevating correctly and must NOT be touched - see MI17DoorGunsModel's
        // "barrel" case) while the RIGHT gun's shootpos was fine at pitchSign=1. So the flip really
        // is per-side, just not the side/reasoning originally guessed: LEFT=-1, RIGHT=1.
        getPositionTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelTransform(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS, RIGHT_BARREL_POS, 1f));
        getPositionTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_POS, -1f));
        getVectorTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelVector(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS, RIGHT_BARREL_POS, 1f));
        getVectorTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelVector(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_POS, -1f));

        // Yaw-only counterpart used for the seats' CameraPos.Transform, so the camera swings with
        // the gun's yaw but stays level through its pitch - see Uh60MinigunEntity for why.
        getPositionTransform().put("RightTurret",
                (Float pt) -> getGunTurretTransform(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS));
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

    // Yaw offset (degrees) from that seat's weapon's configured rest direction
    // ("DefaultBarrelDirection" in mi17_door_guns.json) to its current aim - mirrors SBW's own
    // BoundBonesYaw handling (GeoVehicleRenderer.kt), computed by hand since that renderer path
    // doesn't apply to this vehicle's GeckoLib pipeline.
    public float getGunYawDeg(int seatIndex, float partialTicks) {
        if (getNthEntity(seatIndex) == null) return 0f;
        Vec3 targetVec = getShootVec(seatIndex, partialTicks);
        Vec3 defaultVec = getDefaultBarrelDirection(seatIndex, partialTicks);
        if (targetVec == null || defaultVec == null) return 0f;
        double diffY = Mth.wrapDegrees(-VehicleVecUtils.getYRotFromVector(targetVec) + VehicleVecUtils.getYRotFromVector(defaultVec));
        return (float) -diffY;
    }

    // Pitch offset (degrees), mirroring BoundBonesPitch the same way.
    public float getGunPitchDeg(int seatIndex, float partialTicks) {
        if (getNthEntity(seatIndex) == null) return 0f;
        Vec3 targetVec = getShootVec(seatIndex, partialTicks);
        Vec3 defaultVec = getDefaultBarrelDirection(seatIndex, partialTicks);
        if (targetVec == null || defaultVec == null) return 0f;
        double diffX = Mth.wrapDegrees(-VehicleVecUtils.getXRotFromVector(targetVec) + VehicleVecUtils.getXRotFromVector(defaultVec));
        return (float) -diffX;
    }

    private Matrix4d getGunBarrelTransform(float partialTicks, int seatIndex, Vec3 turretPos, Vec3 barrelPos, float pitchSign) {
        Matrix4d transform = getVehicleTransformWithCustomPitch(partialTicks);
        // turretPos and barrelPos are NOT two independent pivots (unlike a true turret+trunnion
        // gimbal, e.g. Uh60MinigunEntity's minigun, which really does have a separate yaw ring and
        // pitch axis bone). On this model both yaw and pitch are applied to the SAME single
        // "barrel"/"barrel2" bone in MI17DoorGunsModel, which GeckoLib always rotates around that
        // one bone's own fixed pivot - turretPos+barrelPos, i.e. the absolute pivot barrelPos was
        // originally computed relative to (see the constructor comment history / round-6 notes).
        // The previous translate(turretPos) -> rotate(yaw) -> translate(barrelPos) -> rotate(pitch)
        // sequence instead treated barrelPos as a SECOND pivot that orbits around turretPos as yaw
        // changes, which the real single-bone model never does - the barrel's true pivot never
        // moves. That mismatch is invisible at yaw=0 (both formulations agree) and grows with yaw,
        // which is exactly what was reported: ShootPos looked right at rest but desynced from the
        // visible muzzle as the gun tracked a target ("its pivot point is not the same as the PKM
        // in the model so it desyncs as the weapon rotates"). Translating straight to the combined
        // fixed pivot and rotating both axes there - matching the single-bone model - fixes this.
        // ShootPos.Positions itself does NOT need to change: it was already derived relative to
        // this same turretPos+barrelPos point (see the round-8 muzzle-offset derivation notes).
        Vec3 pivot = turretPos.add(barrelPos);
        transform.translate(pivot.x, pivot.y, pivot.z);
        transform.rotate(Axis.YP.rotationDegrees(getGunYawDeg(seatIndex, partialTicks)));
        // Pitch axis: this gun's rest/forward direction is local +X (DefaultBarrelDirection
        // [1,0,0]/[-1,0,0] in the JSON, confirmed by the geo conversion work), NOT +Z like
        // Uh60MinigunEntity's minigun (whose DefaultBarrelDirection is [0,0,1] - that's what the
        // Axis.XP pitch rotation this replaced was actually copied from). A rotation around X
        // leaves the X coordinate untouched and only mixes Y/Z - fine for a Z-forward gun, but for
        // an X-forward one it means the dominant component of ShootPos.Positions (X, ~0.57) never
        // moves under pitch at all, and only the tiny Y/Z offset (~0.03) gets swept - exactly the
        // reported "barely elevates, under 3 degrees" (that residual motion was real, just far too
        // small, since it was coming entirely from a ~0.03-unit arm instead of the ~0.57-unit one).
        // Rotating around Z instead - the axis perpendicular to both "up" (Y) and this gun's own
        // forward (X) - correctly sweeps the dominant X component into Y as the gun pitches. The
        // angle expression itself (pitchSign * -getGunPitchDeg) is unchanged: getGunPitchDeg is
        // positive aiming down / negative aiming up (mirrors Minecraft's own xRot sign), and
        // Axis.ZP.rotationDegrees(θ) rotates local +X toward +Y (up) for θ>0, so θ = -getGunPitchDeg
        // still gives θ>0 (raises the muzzle) exactly when aiming up - the same sign math that was
        // already here, just applied to the correct axis. If elevation still comes out inverted in
        // game, that's a one-line sign flip (drop the leading "-"), not a bigger rethink.
        transform.rotate(Axis.ZP.rotationDegrees(pitchSign * -getGunPitchDeg(seatIndex, partialTicks)));
        return transform;
    }

    private Matrix4d getGunTurretTransform(float partialTicks, int seatIndex, Vec3 turretPos) {
        Matrix4d transform = getVehicleTransformWithCustomPitch(partialTicks);
        transform.translate(turretPos.x, turretPos.y, turretPos.z);
        transform.rotate(Axis.YP.rotationDegrees(getGunYawDeg(seatIndex, partialTicks)));
        return transform;
    }

    private Vec3 getGunBarrelVector(float partialTicks, int seatIndex, Vec3 turretPos, Vec3 barrelPos, float pitchSign) {
        Matrix4d transform = getGunBarrelTransform(partialTicks, seatIndex, turretPos, barrelPos, pitchSign);
        Vector4d root = transform.transform(new Vector4d(0, 0, 0, 1));
        Vector4d target = transform.transform(new Vector4d(0, 0, 1, 1));
        return new Vec3(root.x, root.y, root.z).vectorTo(new Vec3(target.x, target.y, target.z));
    }
}
