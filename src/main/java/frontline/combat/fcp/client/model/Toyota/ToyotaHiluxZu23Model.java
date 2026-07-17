package frontline.combat.fcp.client.model.Toyota;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxZu23Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ToyotaHiluxZu23Model extends VehicleModel<ToyotaHiluxZu23Entity> {

    /** Backward hull kick amplitude (translation). Raise for a heavier lurch. */
    private static final float HULL_LURCH = 12f;
    /** Body pitch amplitude in degrees-scale. Keep small so it doesn't swing the barrel. */
    private static final float HULL_PITCH = 4f;

    @Override
    public ResourceLocation getModelResource(ToyotaHiluxZu23Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/toyota_hilux_zu23.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<ToyotaHiluxZu23Entity> collectTransform(String boneName) {

        VehicleModel.TransformContext<ToyotaHiluxZu23Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<ToyotaHiluxZu23Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        // Hull recoil. SBW's firing shake is only wired to a bone named "base"; this
        // geo has none. The whole vehicle hangs off "root", so binding the shake there
        // does drive the entire model — but an autocannon's recoilShake is tiny
        // (it scales with RecoilTime^4, and 24^4 is ~60x smaller than a tank's 42^4),
        // so the ~0.01-block kick only shows on the thin barrel, not the chunky body.
        //
        // Rather than crank RecoilForce/RecoilTime (which would also blow up the camera
        // and aim shake, since they share those values), reproduce SBW's turn-aware
        // direction math on "root" but amplify the HULL VISUAL: a translation-biased
        // backward lurch with only a little body pitch. The camera/aim shake stays as
        // configured; the body clearly kicks. HULL_LURCH / HULL_PITCH are the knobs.
        if ("root".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float shake = Mth.lerp((float) state.getPartialTick(),
                        (float) vehicle.getRecoilShakeO(), (float) vehicle.getRecoilShake());
                float a = vehicle.getYawWhileShoot();
                float r = (Mth.abs(a) - 90f) / 90f;
                float r2 = Mth.abs(a) <= 90f
                        ? a / 90f
                        : (a < 0 ? -(180f + a) / 90f : (180f - a) / 90f);

                float lurch = shake * HULL_LURCH; // backward hull kick (translation)
                float pitch = shake * HULL_PITCH; // gentle body pitch, not a barrel swing

                bone.setPosX(r2 * lurch * 0.5f);
                bone.setPosZ(r * lurch);
                bone.setRotX(r * pitch * Mth.DEG_TO_RAD);
                bone.setRotZ(r2 * pitch * Mth.DEG_TO_RAD);
            };
        }

        return super.collectTransform(boneName);
    }
}