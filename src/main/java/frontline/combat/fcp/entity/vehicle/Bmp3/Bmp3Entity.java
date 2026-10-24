package frontline.combat.fcp.entity.vehicle.Bmp3;

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

/**
 * BMP-3 family (bmp3, bmp3_cage, bmp3m, brm3k) on one class; the registry name picks the geo and skins.
 * <p>
 * The BMP-3s have two bow PKTs ("pkt" right, "pkt2" left), each aimed by its own seat with the same
 * math as SBW's BoundBones (see KhrizantemaEntity / Uh60MinigunEntity): "PktBarrel"/"Pkt2Barrel"
 * (yaw + pitch about the gun's pivot) carry their ShootPos, "PktTurret"/"Pkt2Turret" (yaw only) their
 * cameras. The BRM-3K has no PKT seats, so its guns stay put.
 */
public class Bmp3Entity extends CamoVehicleBase {

    public static final int PKT_SEAT = 2, PKT2_SEAT = 3;

    /** The "pkt" / "pkt2" bone pivots, in blocks (config convention). */
    private static final Vec3 PKT_PIVOT = new Vec3(1.3060, 1.7096, 2.2478);
    private static final Vec3 PKT2_PIVOT = new Vec3(-1.3060, 1.7096, 2.2478);

    public int INVENTORY_SIZE = 9;

    public Bmp3Entity(EntityType<Bmp3Entity> type, Level world) {
        super(type, world);
        getPositionTransform().put("PktBarrel", (Float pt) -> pktTransform(pt, PKT_SEAT, PKT_PIVOT, true));
        getVectorTransform().put("PktBarrel", (Float pt) -> forward(pktTransform(pt, PKT_SEAT, PKT_PIVOT, true)));
        getPositionTransform().put("PktTurret", (Float pt) -> pktTransform(pt, PKT_SEAT, PKT_PIVOT, false));
        getPositionTransform().put("Pkt2Barrel", (Float pt) -> pktTransform(pt, PKT2_SEAT, PKT2_PIVOT, true));
        getVectorTransform().put("Pkt2Barrel", (Float pt) -> forward(pktTransform(pt, PKT2_SEAT, PKT2_PIVOT, true)));
        getPositionTransform().put("Pkt2Turret", (Float pt) -> pktTransform(pt, PKT2_SEAT, PKT2_PIVOT, false));
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    /** Yaw (degrees) from a bow PKT's rest direction to its gunner's aim; 0 with nobody (or no gun) in the seat. */
    public float pktYaw(int seat, float partialTicks) {
        if (getNthEntity(seat) == null || getGunData(seat) == null) return 0f;
        Vec3 target = getShootVec(seat, partialTicks), rest = getDefaultBarrelDirection(seat, partialTicks);
        if (target == null || rest == null) return 0f;
        return (float) Mth.wrapDegrees(-VehicleVecUtils.getYRotFromVector(target) + VehicleVecUtils.getYRotFromVector(rest)) * -1f;
    }

    /** Pitch (degrees) from a bow PKT's rest direction to its gunner's aim; 0 with nobody in the seat. */
    public float pktPitch(int seat, float partialTicks) {
        if (getNthEntity(seat) == null || getGunData(seat) == null) return 0f;
        Vec3 target = getShootVec(seat, partialTicks), rest = getDefaultBarrelDirection(seat, partialTicks);
        if (target == null || rest == null) return 0f;
        return (float) Mth.wrapDegrees(-VehicleVecUtils.getXRotFromVector(target) + VehicleVecUtils.getXRotFromVector(rest)) * -1f;
    }

    /** Each gun pivots on one bone, so yaw and pitch both turn about its pivot. */
    private Matrix4d pktTransform(float partialTicks, int seat, Vec3 pivot, boolean withPitch) {
        Matrix4d t = getVehicleTransform(partialTicks);
        t.translate(pivot.x, pivot.y, pivot.z);
        t.rotate(Axis.YP.rotationDegrees(pktYaw(seat, partialTicks)));
        if (withPitch) t.rotate(Axis.XP.rotationDegrees(-pktPitch(seat, partialTicks)));
        return t;
    }

    /** The transform's forward (+Z) direction in the world. */
    private static Vec3 forward(Matrix4d t) {
        Vector4d o = t.transform(new Vector4d(0, 0, 0, 1)), f = t.transform(new Vector4d(0, 0, 1, 1));
        return new Vec3(o.x, o.y, o.z).vectorTo(new Vec3(f.x, f.y, f.z));
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
}
