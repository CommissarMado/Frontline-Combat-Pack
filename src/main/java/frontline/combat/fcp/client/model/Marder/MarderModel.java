package frontline.combat.fcp.client.model.Marder;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.T72.T72TrackPaths;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Marder.MarderEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Marder 1A3. The tracks are rigid segments and stay as modelled; the wheels ("wheelR..." on both sides) spin by which
 * side the bone is on. "barrel2" (the Milan launcher) elevates with the gun about its own pivot and "missile2" comes
 * off halfway through the reload (LauncherTube). "dulo2" recoils with the 20 mm.
 * <p>
 * Extends VehicleModel directly (like the Bradley): FCPVehicleModel un-hides transform bones, which would
 * cancel the missile hide.
 */
public class MarderModel extends FCPVehicleModel<MarderEntity> {

    private static final FCPTrackPath TRACK_PATH = MarderTrackPaths.MARDER.reversed();

    @Override
    protected FCPTrackPath getTrackPath() {
        return TRACK_PATH;
    }

    @Override
    public ResourceLocation getModelResource(MarderEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/marder1a3.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<MarderEntity> collectTransform(String boneName) {
        if (boneName.startsWith("wheelR")) {
            VehicleModel.TransformContext<MarderEntity> left = super.collectTransform("wheelL"), right = super.collectTransform("wheelR");
            if (left == null || right == null) return null;
            // GeckoLib negates the geo's x: a negative pivot x is the vehicle's left (+x in the vehicle JSON).
            return (bone, vehicle, state) -> (bone.getPivotX() < 0 ? left : right).transform(bone, vehicle, state);
        }
        return switch (boneName) {
            case "barrel2" -> super.collectTransform("barrel");
            case "missile2" -> (bone, vehicle, state) -> bone.setHidden(vehicle.launcherTubeOff());
            case "dulo2" -> (bone, vehicle, state) -> {
                ModelBoneTransforms.clearRecoilOffsets(bone);
                if (vehicle.getCannonRecoilTime() <= 0 || !"Cannon".equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
                CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.LIGHT);
            };
            default -> super.collectTransform(boneName);
        };
    }
}
