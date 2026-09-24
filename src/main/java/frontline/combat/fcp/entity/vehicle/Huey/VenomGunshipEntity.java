package frontline.combat.fcp.entity.vehicle.Huey;

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

// Dual independent door guns, built the same way as Uh60MinigunEntity (see that class for the
// full explanation of why the vehicle's built-in Turret/PassengerWeaponStation systems don't
// work for two independent guns on a GeckoLib-rendered vehicle). Both guns rest facing forward
// (+Z, confirmed against the actual model - an earlier from-scratch geometric derivation in this
// class guessed sideways/outward and was wrong), exactly like the UH-60's own guns: elevation
// rotates around X, and DefaultBarrelDirection is the same canonical [0,0,1] declared in
// venom_gunship.json's MinigunRight/MinigunLeft. No Z-axis substitution needed anywhere in this
// class - it's the plain UH-60 pattern throughout.
//
// LEFT/RIGHT FIX (bone mapping only - the weapon-name/seat assignment was already correct and is
// untouched): RIGHT_GUN_SEAT (seat 2, "MinigunRight", whose hand-placed seat Position sits at
// NEGATIVE vehicle X) is driven by the "turret2"/"barrel2"/GUN3 bone chain, and LEFT_GUN_SEAT
// (seat 3, positive X) by "turret"/"barrel"/GUN2 - the two geo bone pairs pose to the OPPOSITE
// side from what their own names suggest once Uh-1's baked [0,180,0] ancestor rotation (inherited
// by every bone in the model) is properly composed in, which is also what was putting the
// muzzle/CameraPos pivot on the wrong side of the vehicle entirely.
public class VenomGunshipEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    // Seat 2 = right door gunner, seat 3 = left door gunner - the two seats now sitting right
    // after the two pilot seats (moved there per the Black Hawk's own seat 2/3 gunner
    // convention), carrying over the physical positions/orientations of what used to be the
    // last two seats in venom.json's list.
    private static final int RIGHT_GUN_SEAT = 2;
    private static final int LEFT_GUN_SEAT = 3;

    // Absolute vehicle-space pivot for each gun's yaw/pitch bone (turret1 and barrel1 share the
    // exact same pivot in this geo file - there's no separate barrel offset stage like the
    // UH-60 has, so there's only one Vec3 per side here instead of a turret+barrel pair).
    // RIGHT_TURRET_POS backs seat 2/"MinigunRight" and is now the "turret2"/"barrel2" chain's
    // posed pivot; LEFT_TURRET_POS backs seat 3/"MinigunLeft" and is now "turret1"/"barrel1"'s -
    // see the class comment above for why this is swapped from the bone names' own left/right-
    // sounding labels. Both properly composed through Uh-1's [0,180,0] ancestor rotation (model
    // units / 16, Z negated, THEN rotated by the full ancestor chain - not just a flat /16 of the
    // raw pivot).
    private static final Vec3 RIGHT_TURRET_POS = new Vec3(-1.72205, 1.43412, -0.08277);
    private static final Vec3 LEFT_TURRET_POS = new Vec3(1.72433, 1.43419, -0.08277);

    // The front sensor is no longer driven by hand-rolled Java (see the removed getCameraYawDeg/
    // getCameraPitchDeg/DEFAULT_CAMERA_DIRECTION) - it's now a native "turret"/"barrel" bone pair
    // (see the geo file and VenomGunshipModel), driven entirely by venom_gunship.json's TurretPos/
    // BarrelPos/TurretControllerIndex/TurretPitchRange/TurretYawRange, the same way Oh1Entity and
    // Mh60lEntity need zero turret-related code of their own.

    private float barrelRotationLeft = 0f;
    private float barrelRotationLeftOld = 0f;

    private float barrelRotationRight = 0f;
    private float barrelRotationRightOld = 0f;

    public VenomGunshipEntity(EntityType<VenomGunshipEntity> type, Level world) {
        super(type, world);
        getPositionTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS));
        getPositionTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelTransform(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS));
        getVectorTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelVector(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS));
        getVectorTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelVector(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS));

        // Yaw-only counterpart for the seats' CameraPos.Transform - see Uh60MinigunEntity's
        // "RightTurret"/"LeftTurret" comment for why the camera needs this instead of the full
        // pitched frame (its POSITION should only swing with yaw and stay level as the gun
        // elevates, not orbit around with the pitch too).
        getPositionTransform().put("LeftTurret",
                (Float pt) -> getGunTurretTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS));
        getPositionTransform().put("RightTurret",
                (Float pt) -> getGunTurretTransform(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS));
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

    private Matrix4d getGunBarrelTransform(float partialTicks, int seatIndex, Vec3 turretPos) {
        Matrix4d transform = getVehicleTransformWithCustomPitch(partialTicks);
        transform.translate(turretPos.x, turretPos.y, turretPos.z);
        transform.rotate(Axis.YP.rotationDegrees(getGunYawDeg(seatIndex, partialTicks)));
        // NOT negated here, unlike Uh60MinigunEntity.getGunBarrelTransform - this one extra
        // difference is real, not a copy-paste slip. UH-60's turret/barrel bones sit directly
        // under bones with no ancestor rotation at all, so only the usual GeckoLib-bone-vs-JOML
        // sign flip applies (bone.setRotX(+d) <-> Axis.XP.rotationDegrees(-d)). This vehicle's
        // turret1/barrel1/turret2/barrel2 chain, though, is nested under "Uh-1", which carries a
        // baked [0, 180, 0] rest rotation (see the class comment above) - a 180 degree spin about
        // Y flips both the X and Z axes of everything beneath it, which flips the sense of any
        // X-axis rotation applied further down the chain a SECOND time. That second flip cancels
        // the usual bone-vs-matrix negation, so this transform needs the plain, un-negated
        // getGunPitchDeg to end up physically consistent with how the "barrel1"/"barrel2" bones
        // actually get posed in VenomGunshipModel. Confirmed numerically (not just reasoned about
        // - given how many times "it's obviously X" has been wrong this session): computed the
        // bone's own posed position via full forward kinematics for a few test pitch angles, and
        // compared it against what this exact Matrix4d chain produces for the same angles. Only
        // the un-negated version agreed on which way the muzzle moves as the gun elevates - the
        // negated version (the literal UH-60 copy) moved the ShootPos/muzzle-flash point the
        // opposite way from the visible barrel, which is exactly the "muzzle flash elevates in
        // the wrong direction" bug this was fixing.
        transform.rotate(Axis.XP.rotationDegrees(getGunPitchDeg(seatIndex, partialTicks)));
        return transform;
    }

    private Matrix4d getGunTurretTransform(float partialTicks, int seatIndex, Vec3 turretPos) {
        Matrix4d transform = getVehicleTransformWithCustomPitch(partialTicks);
        transform.translate(turretPos.x, turretPos.y, turretPos.z);
        transform.rotate(Axis.YP.rotationDegrees(getGunYawDeg(seatIndex, partialTicks)));
        return transform;
    }

    private Vec3 getGunBarrelVector(float partialTicks, int seatIndex, Vec3 turretPos) {
        Matrix4d transform = getGunBarrelTransform(partialTicks, seatIndex, turretPos);
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
        barrelRotationLeftOld = barrelRotationLeft;
        if (isWeaponFiring("MinigunLeft")) {
            barrelRotationLeft += 20f;
        }
        barrelRotationRightOld = barrelRotationRight;
        if (isWeaponFiring("MinigunRight")) {
            barrelRotationRight += 20f;
        }
    }

    public float getBarrelRotLeft() { return barrelRotationLeft; }
    public float getBarrelRotLeftOld() { return barrelRotationLeftOld; }
    public float getBarrelRotRight() { return barrelRotationRight; }
    public float getBarrelRotRightOld() { return barrelRotationRightOld; }
}
