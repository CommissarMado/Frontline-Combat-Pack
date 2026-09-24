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

// Identical setup to VenomGunshipEntity - venom_door_guns.geo.json shares the exact same
// corrected bone hierarchy/pivots as venom_gunship.geo.json (44 bones, same turret1/turret2/
// barrel1/barrel2/GUN2/GUN3 gun chain plus the native turret/barrel front-sensor pair, only the
// group5/group7 cube contents differ visually - rocket pods vs bare stubs), so all the constants
// and math below carry over unchanged. See VenomGunshipEntity for the full reasoning: both guns
// rest facing forward (+Z) like the UH-60's, so pitch is a plain X-axis rotation with no
// substitution, and the turret/barrel <-> seat left/right swap (Uh-1's baked [0,180,0] ancestor
// rotation puts each named bone pair on the opposite side from what its own name suggests).
public class VenomDoorGunsEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final int RIGHT_GUN_SEAT = 2;
    private static final int LEFT_GUN_SEAT = 3;

    // See VenomGunshipEntity's class comment for the full derivation. RIGHT_TURRET_POS backs
    // seat 2/"MinigunRight" (now the "turret2"/"barrel2" chain's posed pivot), LEFT_TURRET_POS
    // backs seat 3/"MinigunLeft" ("turret1"/"barrel1"'s) - swapped from the bone names' own
    // left/right-sounding labels, and properly composed through Uh-1's [0,180,0] rotation.
    private static final Vec3 RIGHT_TURRET_POS = new Vec3(-1.72205, 1.43412, -0.08277);
    private static final Vec3 LEFT_TURRET_POS = new Vec3(1.72433, 1.43419, -0.08277);

    // The front sensor is no longer driven by hand-rolled Java (see the removed getCameraYawDeg/
    // getCameraPitchDeg/DEFAULT_CAMERA_DIRECTION) - it's now a native "turret"/"barrel" bone pair
    // (see the geo file and VenomDoorGunsModel), driven entirely by venom_door_guns.json's
    // TurretPos/BarrelPos/TurretControllerIndex/TurretPitchRange/TurretYawRange, the same way
    // Oh1Entity and Mh60lEntity need zero turret-related code of their own.

    private float barrelRotationLeft = 0f;
    private float barrelRotationLeftOld = 0f;

    private float barrelRotationRight = 0f;
    private float barrelRotationRightOld = 0f;

    public VenomDoorGunsEntity(EntityType<VenomDoorGunsEntity> type, Level world) {
        super(type, world);
        getPositionTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS));
        getPositionTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelTransform(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS));
        getVectorTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelVector(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS));
        getVectorTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelVector(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS));

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
        // NOT negated - see VenomGunshipEntity.getGunBarrelTransform for the full reasoning. This
        // vehicle's turret1/barrel1/turret2/barrel2 chain is nested under "Uh-1"'s baked
        // [0, 180, 0] rest rotation, which flips the usual GeckoLib-bone-vs-JOML-matrix sign
        // convention a second time and cancels it out, unlike UH-60's guns (no such ancestor
        // rotation). Confirmed numerically via forward kinematics against the actual geo file,
        // not just carried over from UH-60 - this was the "muzzle flash elevates in the wrong
        // direction" bug.
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
            barrelRotationLeft += 80f;
        }
        barrelRotationRightOld = barrelRotationRight;
        if (isWeaponFiring("MinigunRight")) {
            barrelRotationRight += 80f;
        }
    }

    public float getBarrelRotLeft() { return barrelRotationLeft; }
    public float getBarrelRotLeftOld() { return barrelRotationLeftOld; }
    public float getBarrelRotRight() { return barrelRotationRight; }
    public float getBarrelRotRightOld() { return barrelRotationRightOld; }
}
