package frontline.combat.fcp.client.model.Btr4mv1;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Btr4mv1.BTR4MV1Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * BTR-4MV1 remote turret ("Parus"-style) model.
 *
 * <p>This geo uses non-standard bone names, so the turret/gun bones can't fall through to
 * SBW's base VehicleModel (which only recognises "turret"/"barrel"). They are re-implemented
 * here against the same entity state the base uses:
 * <ul>
 *   <li>{@code basnia}  = turret ring  -> yaw from turretYRot</li>
 *   <li>{@code GUN}     = gun mantlet  -> pitch from turretXRot (clamped to TurretPitchRange);
 *       the ATGM bones PTRK1/PTRK2 are children of GUN and elevate with it automatically.</li>
 * </ul>
 *
 * <p>Wheels {@code wheelL1..4}/{@code wheelR1..4} roll via {@link WheelRotationTransforms}
 * (travel-derived, so it works regardless of SBW's own wheel spin). Their empty parent
 * groups {@code wheelL}/{@code wheelR} are intercepted with a no-op so the base class's
 * lowercase {@code wheel[LR].*} regex doesn't rotate the group on top of the individual wheels.
 */
public class BTR4MV1Model extends FCPVehicleModel<BTR4MV1Entity> {

    /** Wheel rolling radius in blocks (tyre tread diameter ~17.24px / 32). */
    private static final double WHEEL_RADIUS = 0.5388;

    @Override
    public ResourceLocation getModelResource(BTR4MV1Entity a) {
        return new ResourceLocation(FCP.MODID, "geo/btr4mv1.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<BTR4MV1Entity> collectTransform(String boneName) {
        // Turret ring yaw (mirrors base "turret").
        if ("basnia".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float yRot = Mth.lerp(state.getPartialTick(), vehicle.getTurretYRotO(), vehicle.getTurretYRot());
                bone.setRotY(yRot * Mth.DEG_TO_RAD);
            };
        }

        // Gun/mantlet elevation (mirrors base "barrel"); PTRK ATGM bones ride along as children.
        if ("GUN".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float xRot = Mth.lerp(state.getPartialTick(), vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                bone.setRotX(Mth.clamp(-xRot, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
            };
        }

        // Empty wheel-group bones: keep static so the base wheel regex doesn't spin them.
        if ("wheelL".equals(boneName) || "wheelR".equals(boneName)) {
            return (bone, vehicle, state) -> { };
        }

        // The eight road wheels.
        VehicleModel.TransformContext<BTR4MV1Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, WHEEL_RADIUS,
                        "wheelL1", "wheelL2", "wheelL3", "wheelL4",
                        "wheelR1", "wheelR2", "wheelR3", "wheelR4");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
