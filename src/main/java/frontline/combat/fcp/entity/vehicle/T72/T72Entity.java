package frontline.combat.fcp.entity.vehicle.T72;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;

import java.util.Map;

/**
 * The T-72 family and the T-90A on one class (t72a, t72av, t72b, t72b_1985, t72b_1989, t72b2, t72b3,
 * t72b3_2016, t72b3m, t72m2_moderna, t90a); the registry name picks the geo and skins.
 * <p>
 * The PKT coax elevates about its own pivot ("barrel2"), not the main gun's trunnion, and the Moderna's
 * 2A42 about "barrel3". Each gets a transform built like SBW's Barrel transform (turret -> its pivot ->
 * gun pitch): "CoaxBarrel" and "AutocannonBarrel", which their ShootPos use.
 */
public class T72Entity extends CamoVehicleBase {

    public static final String COAX_BARREL = "CoaxBarrel", AUTOCANNON_BARREL = "AutocannonBarrel";

    /** "barrel2" pivot relative to the turret pivot, in blocks (config convention); two coax mounts exist across the geos. */
    private static final Vec3 COAX_FORWARD = new Vec3(-0.4452, -0.1909, 0.6657), COAX_REAR = new Vec3(-0.3980, -0.1880, 0.5167);
    private static final Map<String, Vec3> COAX_PIVOT = Map.of(
            "t72av", COAX_REAR,
            "t72b", COAX_REAR,
            "t72b_1985", COAX_REAR,
            "t90a", new Vec3(-0.3129, -0.3547, 0.4728));
    /** The Moderna's "barrel3" (2A42) pivot relative to the turret pivot. */
    private static final Vec3 AUTOCANNON_PIVOT = new Vec3(-1.1048, 0.5195, -1.3701);

    public int INVENTORY_SIZE = 9;

    public T72Entity(EntityType<T72Entity> type, Level world) {
        super(type, world);
        getPositionTransform().put(COAX_BARREL, (Float pt) -> gunTransform(pt, COAX_PIVOT.getOrDefault(variant(), COAX_FORWARD)));
        getPositionTransform().put(AUTOCANNON_BARREL, (Float pt) -> gunTransform(pt, AUTOCANNON_PIVOT));
    }

    /** Registry path, e.g. "t72b3". */
    public String variant() {
        return EntityType.getKey(getType()).getPath();
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private Matrix4d gunTransform(float partialTicks, Vec3 pivot) {
        Matrix4d t = getTurretTransform(partialTicks);
        t.translate(pivot.x, pivot.y, pivot.z);
        t.rotate(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, getTurretXRotO(), getTurretXRot())));
        return t;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
}
