package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.data.vehicle.subdata.VehicleType;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import frontline.combat.fcp.entity.vehicle.GroundContact;
import frontline.combat.fcp.entity.vehicle.RigidTerrainRest;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Every SBW vehicle tracks a ground-contact shift (non-zero only for landed helicopters, see GroundContact).
 * <p>
 * Ground vehicles also ease their step-ups, on both sides: stepping up lifts the entity the whole step in one tick (stepping
 * down is gradual, gravity), so the pose starts back at the old height and eases to where the rigid hull rests (see
 * RigidTerrainRest). Damage hitboxes, seats and muzzles follow the pose; physics and the terrain collision box stay on the
 * real position.
 */
@Mixin(VehicleEntity.class)
public abstract class VehicleGroundContactMixin implements GroundContact {

    @Unique
    private float fcp$groundShift, fcp$groundShiftO;

    /** Same rate as the attitude's (RigidTerrainRest): height and tilt ease together, so the hull's tail stays on the old ground. */
    @Unique
    private static final double FCP$CLIMB_EASE = 0.1;
    /** Rises per tick from this up to the tallest step are step-ups; anything larger is a teleport. */
    @Unique
    private static final double FCP$STEP_MIN = 0.2, FCP$STEP_MAX = 2.0;
    /** Rest heights this close under the entity are just how the box sits on the ground. */
    @Unique
    private static final double FCP$REST_DEADBAND = 0.15;

    /** Set by RigidTerrainRest each tick it runs: the hull's rest height against the entity's underside. */
    @Unique
    private final RigidTerrainRest.RestMemo fcp$restMemo = new RigidTerrainRest.RestMemo();
    @Unique
    private double fcp$restHeight = Double.NaN, fcp$midHeight = Double.NaN;
    /** How far the hull's middle may sit under the ground while climbing, and the fraction of any more closed per tick. */
    @Unique
    private static final double FCP$MAX_SINK = 0.2, FCP$SINK_EASE = 0.4;
    /** While climbing: the height the pose shows in place of the entity's. */
    @Unique
    private double fcp$shownY;
    @Unique
    private boolean fcp$climbing;
    @Unique
    private float fcp$stepShift, fcp$stepShiftO;

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void fcp$updateGroundShift(CallbackInfo ci) {
        fcp$groundShiftO = fcp$groundShift;
        float target = GroundContact.solve((VehicleEntity) (Object) this);
        // Ease in so take-off/touch-down and tilt changes don't pop.
        fcp$groundShift += (target - fcp$groundShift) * 0.4f;
        if (Math.abs(fcp$groundShift) < 1.0e-4f) fcp$groundShift = 0f;
    }

    /** After the move and the terrain rest but before SBW rebuilds the hitboxes, or they'd pair this tick's height with last tick's shift. */
    @Inject(method = "baseTick", at = @At(value = "INVOKE", remap = false,
            target = "Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;lowHealthWarning()V"))
    private void fcp$easeStep(CallbackInfo ci) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        fcp$stepShiftO = fcp$stepShift;
        VehicleType type = self.getVehicleType();
        double rise = self.getY() - self.yo;
        double rest = fcp$restHeight;
        fcp$restHeight = Double.NaN;
        double mid = Double.isNaN(fcp$midHeight) ? -FCP$STEP_MAX : fcp$midHeight;
        fcp$midHeight = Double.NaN;
        if (rise > FCP$STEP_MAX) {
            fcp$climbing = false;
            fcp$stepShift = fcp$stepShiftO = 0f;
            return;
        }
        if (!fcp$climbing) {
            if (rise <= FCP$STEP_MIN || type == VehicleType.HELICOPTER || type == VehicleType.AIRPLANE || type == VehicleType.AIRSHIP) return;
            // The entity's own interpolation rises by this much across this tick; starting back at the old height cancels it exactly.
            fcp$climbing = true;
            fcp$shownY = self.yo;
        }
        // The entity is lifted the whole step at once, then falls and is pushed up again as its box tilts. The pose ignores all
        // that and goes where the rigid hull rests: part way up while the nose is on the block, the whole step once it has tipped over.
        double target = self.getY() + (Double.isNaN(rest) || rest > -FCP$REST_DEADBAND ? 0 : rest);
        // Driving along the tilted hull climbs by itself, so a long slope doesn't build up a lag behind the ground.
        double yaw = Math.toRadians(self.getYRot());
        double along = -(self.getX() - self.xo) * Math.sin(yaw) + (self.getZ() - self.zo) * Math.cos(yaw);
        fcp$shownY += along * Math.tan(Math.toRadians(-self.getXRot()));
        fcp$shownY += (target - fcp$shownY) * FCP$CLIMB_EASE;
        // Steep inclines add step after step faster than that closes: past this depth the hull's middle catches up quickly.
        double lowest = self.getY() + mid - FCP$MAX_SINK;
        if (fcp$shownY < lowest) fcp$shownY += (lowest - fcp$shownY) * FCP$SINK_EASE;
        double shift = Mth.clamp(fcp$shownY - self.getY(), -FCP$STEP_MAX, 0);
        fcp$shownY = self.getY() + shift;
        fcp$stepShift = (float) shift;
        if (target == self.getY() && shift > -0.01) {
            fcp$climbing = false;
            fcp$stepShift = 0f;
        }
    }

    @Override
    public RigidTerrainRest.RestMemo fcp$restMemo() {
        return fcp$restMemo;
    }

    @Override
    public void fcp$setRestHeight(double height, double midHeight) {
        fcp$restHeight = height;
        fcp$midHeight = midHeight;
    }

    /** SBW built this tick's hitboxes on the shifted transform. The terrain collision box goes back to the real position; the rest stay with the visible hull. */
    @Inject(method = "updateOBB", at = @At("RETURN"), remap = false)
    private void fcp$collisionKeepsRealPosition(CallbackInfo ci) {
        if (fcp$stepShift == 0f) return;
        OBB collision = ((VehicleEntity) (Object) this).getCollisionOBB();
        if (collision != null) collision.center.y -= fcp$stepShift;
    }

    /** Landed helicopters: our geometric settle replaces SBW's terrain tilt. */
    @Inject(method = "terrainCompact", at = @At("HEAD"), cancellable = true, remap = false)
    private void fcp$settleOnGear(List<Vec3> positions, CallbackInfo ci) {
        if (GroundContact.settle((VehicleEntity) (Object) this)) ci.cancel();
    }

    @Override
    public float fcp$groundOffset(float partialTick) {
        return Mth.lerp(partialTick, fcp$groundShiftO, fcp$groundShift) + Mth.lerp(partialTick, fcp$stepShiftO, fcp$stepShift);
    }
}
