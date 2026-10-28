package frontline.combat.fcp.client.model.T72;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Puma.PumaTrackPaths;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.T72.T72Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;

/**
 * One model for the T-72 family and the T-90A (geo by registry name). No track animation.
 * <ul>
 *   <li>"barrel2" (PKT coax) and the Moderna's "barrel3" (2A42) elevate with the main gun about their own pivots.</li>
 *   <li>"GUN" recoils with the 125 mm (same travel as the Abrams), the Moderna's "GUN2" with the 2A42.</li>
 *   <li>"weaponstationturret"/"weaponstationbarrel" are the commander's cupola (SBW's passenger weapon
 *       station). Most of the geos model the cupola turned 180 degrees in its rest pose; SBW sets the bone
 *       rotation outright, so that rest yaw is added back, and the gun's pitch is mirrored under it.</li>
 * </ul>
 */
public class T72Model extends FCPVehicleModel<T72Entity> {

    private static final FCPTrackPath TRACK_PATH = T72TrackPaths.T72.reversed();

    @Override
    protected FCPTrackPath getTrackPath() {
        return TRACK_PATH;
    }

    @Override
    public ResourceLocation getModelResource(T72Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + animatable.variant() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<T72Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "barrel2", "barrel3" -> super.collectTransform("barrel");
            case "GUN" -> recoil("Cannon", CannonRecoilTransforms.Profile.ABRAMS);
            case "GUN2" -> recoil("Autocannon", CannonRecoilTransforms.Profile.LIGHT);
            case "weaponstationturret" -> {
                VehicleModel.TransformContext<T72Entity> yaw = super.collectTransform("passengerWeaponStationYaw");
                yield (bone, vehicle, state) -> {
                    if (yaw != null) yaw.transform(bone, vehicle, state);
                    bone.setRotY(bone.getRotY() + bone.getInitialSnapshot().getRotY());
                };
            }
            case "weaponstationbarrel" -> {
                VehicleModel.TransformContext<T72Entity> pitch = super.collectTransform("passengerWeaponStationPitch");
                yield (bone, vehicle, state) -> {
                    if (pitch != null) pitch.transform(bone, vehicle, state);
                    CoreGeoBone mount = bone.getParent();
                    // Under a cupola turned 180 degrees the bone's X axis points the other way.
                    if (mount != null) bone.setRotX(bone.getRotX() * Mth.cos(mount.getInitialSnapshot().getRotY()));
                };
            }
            default -> super.collectTransform(boneName);
        };
    }

    private VehicleModel.TransformContext<T72Entity> recoil(String weapon, CannonRecoilTransforms.Profile profile) {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) return;
            if (!weapon.equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
            CannonRecoilTransforms.apply(bone, vehicle, profile);
        };
    }
}
