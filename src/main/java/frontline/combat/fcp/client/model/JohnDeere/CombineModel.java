package frontline.combat.fcp.client.model.JohnDeere;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CombineEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class CombineModel extends FCPVehicleModel<CombineEntity> {

    /** Reel speed in Farm mode: about 40 rpm. */
    private static final double REEL_DEG_PER_TICK = 40.0 * 360.0 / 60.0 / 20.0;

    @Override
    public ResourceLocation getModelResource(CombineEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(FCP.MODID, "geo/combine.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    private static float steering(CombineEntity v, float partialTick) {
        return Mth.lerp(partialTick, v.getPrevSteeringAngle(), v.getSteeringAngle());
    }

    @Override
    public @Nullable VehicleModel.TransformContext<CombineEntity> collectTransform(String boneName) {
        // Combine: three wheel sizes. Radii in blocks, read from combine.geo.json (px / 16).
        // A combine steers with its REAR wheels, turned against the turn; the header's gauge wheels (ahead of the drive
        // axle) trail with it. The big front drive wheels only roll: they keep the Turn bones' names from the geo, but are
        // held straight (steer 0, which also clears the bones' modelled 180 degree turn, as the old steering did).
        switch (boneName) {
            case "WheelL0Turn", "WheelR0Turn":
                return WheelRotationTransforms.matchTurn(boneName, boneName, 1.081, 0f, (v, pt) -> 0f);
            case "WheelL0", "WheelR0":
                return WheelRotationTransforms.matchTurn(boneName, boneName, 0.813, 30f, (v, pt) -> -steering(v, pt));
            case "WheelL1", "WheelR1":
                return WheelRotationTransforms.matchTurn(boneName, boneName, 0.432, 30f, CombineModel::steering);
            default:
                break;
        }
        // Unloading auger: swings out 50 degrees (+50 on Y) while switched on.
        if (boneName.equals("spout")) {
            return (bone, vehicle, state) -> bone.setRotY(50f * Mth.DEG_TO_RAD * vehicle.augerOut(state.getPartialTick()));
        }
        // The reel on the header turns while harvesting (Farm mode).
        VehicleModel.TransformContext<CombineEntity> reel =
                WheelRotationTransforms.spinningWhile(boneName, REEL_DEG_PER_TICK, CombineEntity::isFarming, "animation");
        if (reel != null) return reel;

        return super.collectTransform(boneName);
    }
}
