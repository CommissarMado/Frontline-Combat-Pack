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

// Same dual M134 minigun door-gun setup as HueyM134DoorGunsEntity - see that class for the full
// derivation of the dual-custom architecture (both "turret"/"barrel" (LEFT) and "turret2"/
// "barrel2" (RIGHT) get their own custom Matrix4d transform) and the spin-cluster barrel-rotation
// tracking. This variant just adds rocket pods (a pilot-seat "Rocket" weapon, wired purely in the
// vehicle JSON - no geometry or Java difference from the door-guns-only variant, since
// huey_m134_gunship.geo.json shares the identical turret/barrel/turret2/barrel2/minigun bones and
// pivots as huey_m134.geo.json).
public class HueyM134GunshipEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final int RIGHT_GUN_SEAT = 2;
    private static final int LEFT_GUN_SEAT = 3;

    // Posed pivots of "turret2" (RIGHT) and "turret" (LEFT), derived via forward kinematics
    // against huey_m134.geo.json / huey_m134_gunship.geo.json (identical gun-bone pivots in both).
    // LEFT_TURRET_POS is the same value this vehicle's TurretPos used to carry, back when the LEFT
    // gun was driven by the native turret system.
    private static final Vec3 RIGHT_TURRET_POS = new Vec3(-1.64582, 1.31832, -0.39268);
    private static final Vec3 LEFT_TURRET_POS = new Vec3(1.64095, 1.31832, -0.39268);

    // See HueyM60DoorGunsEntity for the full derivation - barrel2's/barrel's pivot sits at a small
    // but real offset from turret2's/turret's, applied as an intermediate translate between the
    // yaw and pitch rotations, matching the native VehicleVecUtils.getBarrelTransform's
    // TurretPos -> yaw -> BarrelPos -> pitch chain. LEFT_BARREL_OFFSET is the same value this
    // vehicle's BarrelPos used to carry.
    private static final Vec3 RIGHT_BARREL_OFFSET = new Vec3(0.0375, 0.0, -0.01875);
    private static final Vec3 LEFT_BARREL_OFFSET = new Vec3(-0.0375, 0, -0.01875);

    private float barrelRotationLeft = 0f;
    private float barrelRotationLeftOld = 0f;

    private float barrelRotationRight = 0f;
    private float barrelRotationRightOld = 0f;

    public HueyM134GunshipEntity(EntityType<HueyM134GunshipEntity> type, Level world) {
        super(type, world);
        getPositionTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_OFFSET));
        getPositionTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelTransform(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS, RIGHT_BARREL_OFFSET));
        getVectorTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelVector(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_OFFSET));
        getVectorTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelVector(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS, RIGHT_BARREL_OFFSET));
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

    // Yaw NOT negated, pitch negated - matches the currently-pushed, confirmed-working
    // VenomDoorGunsEntity.getGunBarrelTransform (re-checked against the fresh clone). See
    // HueyM60DoorGunsEntity for the full note.
    private Matrix4d getGunBarrelTransform(float partialTicks, int seatIndex, Vec3 turretPos, Vec3 barrelOffset) {
        Matrix4d transform = getVehicleTransformWithCustomPitch(partialTicks);
        transform.translate(turretPos.x, turretPos.y, turretPos.z);
        transform.rotate(Axis.YP.rotationDegrees(getGunYawDeg(seatIndex, partialTicks)));
        transform.translate(barrelOffset.x, barrelOffset.y, barrelOffset.z);
        transform.rotate(Axis.XP.rotationDegrees(-getGunPitchDeg(seatIndex, partialTicks)));
        return transform;
    }

    private Matrix4d getGunTurretTransform(float partialTicks, int seatIndex, Vec3 turretPos) {
        Matrix4d transform = getVehicleTransformWithCustomPitch(partialTicks);
        transform.translate(turretPos.x, turretPos.y, turretPos.z);
        transform.rotate(Axis.YP.rotationDegrees(getGunYawDeg(seatIndex, partialTicks)));
        return transform;
    }

    private Vec3 getGunBarrelVector(float partialTicks, int seatIndex, Vec3 turretPos, Vec3 barrelOffset) {
        Matrix4d transform = getGunBarrelTransform(partialTicks, seatIndex, turretPos, barrelOffset);
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
        if (isWeaponFiring("M134Left")) {
            barrelRotationLeft += 80f;
        }
        barrelRotationRightOld = barrelRotationRight;
        if (isWeaponFiring("M134Right")) {
            barrelRotationRight += 80f;
        }
    }

    public float getBarrelRotLeft() { return barrelRotationLeft; }
    public float getBarrelRotLeftOld() { return barrelRotationLeftOld; }
    public float getBarrelRotRight() { return barrelRotationRight; }
    public float getBarrelRotRightOld() { return barrelRotationRightOld; }
}
