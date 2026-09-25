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

// Dual M60 door guns on the Huey skeleton, both driven by custom Matrix4d transforms - same
// dual-custom architecture as the confirmed-working VenomGunshipEntity/VenomDoorGunsEntity
// (neither door gun relies on SBW's native turret system; TurretPos/BarrelPos/
// TurretControllerIndex have been removed from the vehicle JSON entirely, since nothing
// references the literal native "Turret"/"Barrel" transform keys anymore - see huey_m60_door_guns.json).
//
// Both "turret"/"turret2" sit as DIRECT children of "Uh-1" (unlike Venom's front sensor, which
// was nested an extra level under "group8"), so their net ancestor rotation is a plain Ry(180)
// from Uh-1's own baked [0,180,0] - never the problematic Ry(90)/Ry(270) case that turns local-X
// pitch into a boresight twist. That means neither gun needs the "mount"/"barrelMount" bone-
// insertion fix Venom's camera needed; this is architecturally the same as the UH-60's guns.
//
// LEFT/RIGHT: "turret"/"barrel" sits at POSITIVE vehicle X once composed through Uh-1's ancestor
// rotation (confirmed via forward kinematics against this exact geo file) - same sign convention
// as Venom's LEFT_TURRET_POS - so it's the LEFT gun (seat 3). "turret2"/"barrel2" is NEGATIVE X,
// the RIGHT gun (seat 2). Both now get their own custom Matrix4d transform below, mirroring
// VenomGunshipEntity's LeftBarrel/RightBarrel pair exactly.
public class HueyM60DoorGunsEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final int RIGHT_GUN_SEAT = 2;
    private static final int LEFT_GUN_SEAT = 3;

    // Posed pivots of "turret2" (RIGHT) and "turret" (LEFT), composed through Uh-1's [0,180,0]
    // ancestor rotation - derived via full forward kinematics against huey_m60.geo.json (model
    // units / 16, Z negated, then rotated by the ancestor chain), the same method validated for
    // Venom's own turret1/turret2. LEFT_TURRET_POS is the same value this vehicle's TurretPos
    // used to carry, back when the LEFT gun was driven by the native turret system.
    private static final Vec3 RIGHT_TURRET_POS = new Vec3(-1.65043, 1.29991, -0.38736);
    private static final Vec3 LEFT_TURRET_POS = new Vec3(1.64555, 1.28741, -0.38736);

    // Unlike Venom (where turret1/barrel1 and turret2/barrel2 share the exact same pivot, so a
    // single translate covers both), this geo's barrel2/barrel sit at a small but real offset from
    // their own turret2/turret pivot (composed through the same ancestor chain). Applied AFTER the
    // yaw rotation and BEFORE the pitch rotation below, mirroring exactly how the native
    // VehicleVecUtils.getBarrelTransform chains TurretPos -> yaw -> BarrelPos -> pitch.
    // LEFT_BARREL_OFFSET is the same value this vehicle's BarrelPos used to carry.
    private static final Vec3 RIGHT_BARREL_OFFSET = new Vec3(-0.00625, -0.03125, 0.0);
    private static final Vec3 LEFT_BARREL_OFFSET = new Vec3(0, -0.03125, 0);

    public HueyM60DoorGunsEntity(EntityType<HueyM60DoorGunsEntity> type, Level world) {
        super(type, world);
        getPositionTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelTransform(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_OFFSET));
        getPositionTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelTransform(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS, RIGHT_BARREL_OFFSET));
        getVectorTransform().put("LeftBarrel",
                (Float pt) -> getGunBarrelVector(pt, LEFT_GUN_SEAT, LEFT_TURRET_POS, LEFT_BARREL_OFFSET));
        getVectorTransform().put("RightBarrel",
                (Float pt) -> getGunBarrelVector(pt, RIGHT_GUN_SEAT, RIGHT_TURRET_POS, RIGHT_BARREL_OFFSET));

        // Yaw-only pivot for each seat's CameraPos - see VenomGunshipEntity's "LeftTurret"/
        // "RightTurret" comment for why the camera needs this instead of the full pitched frame.
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

    // Yaw NOT negated, pitch negated - this is the exact sign convention in the currently-pushed,
    // confirmed-working VenomGunshipEntity/VenomDoorGunsEntity.getGunBarrelTransform (re-checked
    // against the fresh clone of this repo, since the user finished that fix manually). Uh-1's
    // baked [0, 180, 0] ancestor rotation flips the usual GeckoLib-bone-vs-JOML-matrix sign
    // convention a second time for X-axis (pitch) rotations, cancelling it out, but leaves the
    // Y-axis (yaw) convention alone - this applies identically to both turret/barrel (LEFT) and
    // turret2/barrel2 (RIGHT), since both sit under the same kind of Ry(180) ancestor.
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
