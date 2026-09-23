package frontline.combat.fcp.entity.vehicle.Uh60;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector4d;

public class Uh60MinigunEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    // Two fully independent door guns. Neither the vehicle's built-in Turret
    // system nor its PassengerWeaponStation system is used for aiming - both
    // turned out to be dead ends for this vehicle:
    //
    // - Turret is a single slot (one TurretControllerIndex, one turretYRot/
    //   turretXRot pair) - fine for one gun, but there's no second one.
    // - PassengerWeaponStation looked like the answer, but SBW's own
    //   getGunTransform() (VehicleVecUtils.kt) builds its position by
    //   translating PassengerWeaponStationPos INSIDE the primary Turret's
    //   already-rotated frame (only the rotation angle itself cancels the
    //   turret's contribution back out - the position does not), so it's
    //   physically mounted on/relative to the main turret and swings around
    //   with it. That's correct for a coaxial gun riding the main turret, but
    //   wrong for two guns on opposite sides of the aircraft.
    // - SBW's real "N independent guns on one vehicle" mechanism is
    //   "BoundBones"/"BoundBonesYaw"/"BoundBonesPitch" (see AC-130H, the
    //   FH-77BW SPG), but that's only applied by GeoVehicleRenderer.kt's
    //   renderer, which is paired with the newer non-GeckoLib
    //   "BedrockVehicleModel" pipeline. Every FCP vehicle, this one included,
    //   is built on the older GeckoLib VehicleModel/VehicleRenderer pipeline
    //   instead, which never calls that renderer code - so those JSON fields
    //   are silently inert here.
    //
    // So both guns are driven by hand, replicating BoundBones' own math
    // (see getGunYawDeg/getGunPitchDeg below) directly from each seat's own
    // aim (vehicle.getShootVec(seatIndex, ticks) - the seat occupant's
    // current look, clamped to that seat's MinYaw/MaxYaw/MinPitch/MaxPitch in
    // uh60_minigun.json) against that seat's weapon's rest direction
    // (vehicle.getDefaultBarrelDirection(seatIndex, ticks), reading
    // "DefaultBarrelDirection" off the weapon in the same JSON - both guns
    // rest facing forward, [0,0,1], matching the geo's actual rest pose).
    // Uh60MinigunModel drives the "turret"/"barrel" (right) and
    // "turret2"/"barrel2" (left) bones straight from these two methods.
    //
    // The camera and the fired shot need to track that same rotation, not
    // just the bone mesh, so this class also registers two custom transforms,
    // "RightBarrel" and "LeftBarrel" (positionTransform/vectorTransform,
    // inherited from VehicleEntity - normally populated only with the fixed
    // set of built-in names like "Turret"/"Barrel"/"WeaponStationBarrel"),
    // built from the exact same getGunYawDeg/getGunPitchDeg values, rooted
    // directly in the vehicle's own transform (never nested under anything
    // else). uh60_minigun.json's seat 2/3 CameraPos and the MinigunLeft/
    // MinigunRight weapons' ShootPos point at "LeftBarrel"/"RightBarrel" to
    // pick these up.
    private static final int LEFT_GUN_SEAT = 2;
    private static final int RIGHT_GUN_SEAT = 3;

    // Absolute vehicle-space pivot for each gun's yaw bone, and the pitch
    // bone's position relative to that - the same numbers uh60_minigun.json
    // used for TurretPos/BarrelPos (right) and PassengerWeaponStationPos/
    // PassengerWeaponStationBarrelPos (left) throughout this whole build.
    private static final Vec3 RIGHT_TURRET_POS = new Vec3(1.95673, 1.89468, 1.91293);
    private static final Vec3 RIGHT_BARREL_POS = new Vec3(-0.0031, 0.0569, 0.14245);
    private static final Vec3 LEFT_TURRET_POS = new Vec3(-1.95673, 1.89468, 1.91293);
    private static final Vec3 LEFT_BARREL_POS = new Vec3(0.0031, 0.0569, 0.14245);

    private float barrelRotationRight = 0f;
    private float barrelRotationRightOld = 0f;

    private float barrelRotationLeft = 0f;
    private float barrelRotationLeftOld = 0f;

    public Uh60MinigunEntity(EntityType<Uh60MinigunEntity> type, Level world) {
        super(type, world);
        // positionTransform/vectorTransform are populated by the superclass's
        // own registerTransforms() during super(type, world), so it's safe to
        // add our own entries right after.
        getPositionTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelTransform(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS, RIGHT_BARREL_POS));
        getPositionTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_POS));
        getVectorTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelVector(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS, RIGHT_BARREL_POS));
        getVectorTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelVector(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_POS));

        // "RightTurret"/"LeftTurret": a YAW-ONLY counterpart to "RightBarrel"/"LeftBarrel",
        // used for the seats' CameraPos.Transform. Mirrors SBW's own built-in "Turret" vs
        // "Barrel" split (VehicleVecUtils.getTurretTransform/getBarrelTransform) - vanilla
        // turret seats (e.g. the M1A2 gunner) point CameraPos.Transform at the yaw-only
        // "Turret" pivot and only point Direction at the full yaw+pitch "Barrel", so the
        // camera's rotation follows the whole aim but its POSITION only swings with yaw,
        // staying level as the gun elevates - like a head sitting on a turret ring instead
        // of being bolted to the barrel itself. "RightBarrel"/"LeftBarrel" bake pitch into
        // the position too (needed for ShootPos and the bone rotation), which is exactly
        // why the camera pivot didn't match the turret pivot and swung around oddly with
        // elevation - it was using the pitched frame instead of this one.
        getPositionTransform().put("RightTurret",
                (Float pt) -> getGunTurretTransform(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS));
        getPositionTransform().put("LeftTurret",
                (Float pt) -> getGunTurretTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS));
    }

    // Yaw offset (degrees) from that seat's weapon's rest direction to its
    // current aim - mirrors GeoVehicleRenderer.kt's BoundBonesYaw handling
    // (diffY = wrapDegrees(-yRot(target) + yRot(default)); result = -diffY).
    public float getGunYawDeg(int seatIndex, float partialTicks) {
        if (getNthEntity(seatIndex) == null) return 0f;
        Vec3 targetVec = getShootVec(seatIndex, partialTicks);
        Vec3 defaultVec = getDefaultBarrelDirection(seatIndex, partialTicks);
        if (targetVec == null || defaultVec == null) return 0f;
        double diffY = Mth.wrapDegrees(-VehicleVecUtils.getYRotFromVector(targetVec) + VehicleVecUtils.getYRotFromVector(defaultVec));
        return (float) -diffY;
    }

    // Pitch offset (degrees), mirroring GeoVehicleRenderer.kt's
    // BoundBonesPitch handling the same way.
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
        transform.translate(turretPos.x, turretPos.y, turretPos.z);
        transform.rotate(Axis.YP.rotationDegrees(getGunYawDeg(seatIndex, partialTicks)));
        transform.translate(barrelPos.x, barrelPos.y, barrelPos.z);
        // NEGATED here on purpose - this is SBW's own established convention, not a guess.
        // VehicleModel.kt's built-in "barrel" bone case sets bone.rotX = -turretXRot, while
        // VehicleVecUtils.getBarrelTransform() (the Matrix4d equivalent, used for the built-in
        // "Barrel" position/vector transform) rotates with Axis.XP.rotationDegrees(+turretXRot)
        // for that SAME turretXRot value - i.e. GeckoLib's bone.setRotX and JOML's
        // Axis.XP.rotationDegrees turn opposite ways for the same signed angle. Our own "barrel"/
        // "barrel2" bone cases in Uh60MinigunModel already use +getGunPitchDeg(...) un-negated
        // (confirmed visually correct - the guns elevate the right way), so to make the muzzle
        // tip/camera pivot rotate the SAME physical direction as that already-correct bone, this
        // Matrix4d chain needs the negated value, matching the bone/matrix pairing SBW uses
        // everywhere else. Without this, ShootPos moved with the gun on the wrong axis sense
        // (or barely visibly, depending on how it combined with each side's mirroring).
        transform.rotate(Axis.XP.rotationDegrees(-getGunPitchDeg(seatIndex, partialTicks)));
        return transform;
    }

    // Same root as getGunBarrelTransform but stops after the yaw rotation - no barrelPos
    // translate, no pitch rotation. This is the "camera sits on the turret ring" pivot;
    // see the "RightTurret"/"LeftTurret" registration above for why the camera needs this
    // instead of the full pitched barrel frame.
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

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // NOTE: barrelRotationRight/Left are intentionally left to grow UNBOUNDED here (no
        // wrap-at-360 reset) - same idiom as the vehicle's own propeller rotation. Wrapping the
        // stored value and then Mth.lerp-ing between an old value just under 360 and a new value
        // just over 0 makes the interpolated render sweep backward through the whole circle for
        // one tick's worth of frames, which looks exactly like a random snap. Letting it grow
        // unboundedly and only ever converting to radians at the point of use (see
        // Uh60MinigunModel's "GUN"/"GUN2" cases) avoids that entirely; float precision is more
        // than adequate for how large this can realistically get in a play session.
        //
        // Spin state used to be driven off "did this weapon's ammo count go down this tick",
        // but MinigunRight/MinigunLeft don't declare a Magazine in uh60_minigun.json, so SBW
        // treats them as backpack-ammo weapons (GunData.useBackpackAmmo() - magazine <= 0): the
        // number that heuristic was reading is backupAmmoCount, a periodically-rescanned count
        // of the SAME shared rifle_ammo pool in the vehicle's cargo, not a per-weapon "rounds
        // fired" counter. Both guns' backupAmmoCount gets resynced together - notably on
        // removePassenger() when either seat is vacated - so whichever gun didn't fire could
        // still see its own ammo reading drop (chasing the other gun's consumption) and spin,
        // with no relation to whether it actually fired. That's the "GUN2 moves when only the
        // right gun fired"/"moves on its own after getting out" bug.
        //
        // isWeaponFiring(weaponName) is SBW's own per-weapon "is this gun's shot/muzzle timer
        // currently active" flag (GunData.shootTimer > 0, the same one the fire sound volume
        // and pitch are driven from) - it's keyed to that specific weapon slot, not the shared
        // ammo pool, so it can't cross-trigger the other gun and doesn't care about anything
        // that happens on dismount.
        barrelRotationRightOld = barrelRotationRight;
        if (isWeaponFiring("MinigunRight")) {
            barrelRotationRight += 80f;
        }

        barrelRotationLeftOld = barrelRotationLeft;
        if (isWeaponFiring("MinigunLeft")) {
            barrelRotationLeft += 80f;
        }
    }

    public float getBarrelRotRight() {
        return barrelRotationRight;
    }

    public float getBarrelRotRightOld() {
        return barrelRotationRightOld;
    }

    public float getBarrelRotLeft() {
        return barrelRotationLeft;
    }

    public float getBarrelRotLeftOld() {
        return barrelRotationLeftOld;
    }

}
