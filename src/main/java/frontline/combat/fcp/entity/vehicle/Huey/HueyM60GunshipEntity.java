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

// Same dual M60 door-gun setup as HueyM60DoorGunsEntity - both guns driven by custom Matrix4d
// transforms, mirroring VenomGunshipEntity's LeftBarrel/RightBarrel pair (see that class for the
// full derivation). This variant just adds rocket pods (a pilot-seat "Rocket" weapon, wired
// purely in the vehicle JSON - no geometry or Java difference from the door-guns-only variant,
// since huey_m60_gunship.geo.json shares the identical turret/barrel/turret2/barrel2 bones and
// pivots as huey_m60.geo.json).
public class HueyM60GunshipEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final int RIGHT_GUN_SEAT = 2;
    private static final int LEFT_GUN_SEAT = 3;

    private static final Vec3 RIGHT_TURRET_POS = new Vec3(-1.65043, 1.29991, -0.38736);
    private static final Vec3 LEFT_TURRET_POS = new Vec3(1.64555, 1.28741, -0.38736);

    // See HueyM60DoorGunsEntity for the full derivation - barrel2/barrel's pivot sits at a small
    // but real offset from turret2's/turret's, unlike Venom's coincident turret/barrel pivots, so
    // it's applied as an intermediate translate between the yaw and pitch rotations, matching the
    // native VehicleVecUtils.getBarrelTransform's TurretPos -> yaw -> BarrelPos -> pitch chain.
    private static final Vec3 RIGHT_BARREL_OFFSET = new Vec3(-0.00625, -0.03125, 0.0);
    private static final Vec3 LEFT_BARREL_OFFSET = new Vec3(0, -0.03125, 0);

    public HueyM60GunshipEntity(EntityType<HueyM60GunshipEntity> type, Level world) {
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
    // VenomGunshipEntity.getGunBarrelTransform (re-checked against the fresh clone). See
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
}
