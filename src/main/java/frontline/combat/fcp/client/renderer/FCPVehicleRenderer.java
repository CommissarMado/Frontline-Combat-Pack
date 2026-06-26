package frontline.combat.fcp.client.renderer;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.core.animatable.GeoAnimatable;

/**
 * FCPVehicleRenderer — vehicle renderer with per-vehicle tilt control.
 *
 * SBW conforms a vehicle to the terrain under it (terrainCompact): pitch is the nose
 * up/down on hills, roll is the side-to-side lean on slopes. The side lean is the
 * part that reads as immersion-breaking "tilt", so the tilt dial scales ROLL only.
 * Hill pitch is left at full strength by default, so dialing tilt down does NOT
 * flatten the vehicle going up or down hills.
 *
 *   tiltStrength(): 0.0 = no side lean, 1.0 = SBW default, &gt;1.0 = exaggerated
 *
 * Override tiltStrength() for the side lean. Pitch (hills) is full unless you also
 * override pitchStrength(). Yaw (facing) is never scaled.
 *
 * <pre>
 *   public class FooRenderer extends FCPVehicleRenderer&lt;FooEntity&gt; {
 *       {@literal @Override} protected float tiltStrength() { return 0.35f; } // gentle lean, hills intact
 *   }
 * </pre>
 */
public abstract class FCPVehicleRenderer<T extends VehicleEntity & GeoAnimatable> extends VehicleRenderer<T> {

    protected FCPVehicleRenderer(EntityRendererProvider.Context ctx, VehicleModel<T> model) {
        super(ctx, model);
    }

    /** Side-to-side lean (roll) multiplier: 0 = flat, 1 = SBW default, &gt;1 = exaggerated. */
    protected float tiltStrength() {
        return 1.0f;
    }

    /** Banking (roll, side-to-side) multiplier; defaults to {@link #tiltStrength()}. */
    protected float rollStrength() {
        return tiltStrength();
    }

    /**
     * Nose pitch (up/down on hills) multiplier. Defaults to 1.0 (full) so terrain
     * pitch is preserved — override only if you specifically want flatter hill
     * response on this vehicle.
     */
    protected float pitchStrength() {
        return 1.0f;
    }

    /**
     * Height (blocks) the tilt pivots around. 0 leans around the model base. Set this
     * to the vehicle's RotateOffsetHeight (from its VehicleData JSON) to match SBW's
     * pivot exactly at tiltStrength 1.0.
     */
    protected double tiltPivotHeight() {
        return 0.0;
    }

    @Override
    public void vehicleAxis(T entity, PoseStack poseStack, float entityYaw, float partialTicks) {
        float py = (float) tiltPivotHeight();

        // Yaw — never scaled (facing must stay correct).
        poseStack.rotateAround(Axis.YP.rotationDegrees(-entityYaw), 0f, py, 0f);

        // Pitch and roll — SBW's own values, scaled per vehicle.
        float pitch = entity.getPitch(partialTicks) * pitchStrength();
        if (pitch != 0f) {
            poseStack.rotateAround(Axis.XP.rotationDegrees(pitch), 0f, py, 0f);
        }

        float roll = entity.getRoll(partialTicks) * rollStrength();
        if (roll != 0f) {
            poseStack.rotateAround(Axis.ZP.rotationDegrees(roll), 0f, py, 0f);
        }
    }
}