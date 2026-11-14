package frontline.combat.fcp.client.model.Type10;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Type10.Type10Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Type 10. Tracks scroll along Type10TrackPaths.
 * <ul>
 *   <li>Road wheels: the geo names them "WheelL0".."WheelR6", which SBW's wheel rule (lowercase "wheelL"/"wheelR")
 *       never matched, so they're mapped onto it here.</li>
 *   <li>"GUN" recoils with the 120 mm (the Abrams' straight travel).</li>
 *   <li>"STVOlm2" is the commander's open-top M2 mount, one bone on SBW's passenger weapon station: it takes both the
 *       station's yaw and its pitch (like the LAV-25's pintle M240).</li>
 * </ul>
 */
public class Type10Model extends FCPVehicleModel<Type10Entity> {

    @Override
    public ResourceLocation getModelResource(Type10Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(FCP.MODID, "geo/type10.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    protected FCPTrackPath getTrackPath() {
        return Type10TrackPaths.TYPE10.reversed();
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Type10Entity> collectTransform(String boneName) {
        if (boneName.matches("Wheel[LR]\\d+")) return super.collectTransform("w" + boneName.substring(1));
        return switch (boneName) {
            case "GUN" -> (bone, vehicle, state) -> {
                ModelBoneTransforms.clearRecoilOffsets(bone);
                if (vehicle.getCannonRecoilTime() <= 0 || !"Cannon".equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
                CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.ABRAMS);
            };
            case "STVOlm2" -> {
                VehicleModel.TransformContext<Type10Entity> yaw = super.collectTransform("passengerWeaponStationYaw");
                VehicleModel.TransformContext<Type10Entity> pitch = super.collectTransform("passengerWeaponStationPitch");
                yield (bone, vehicle, state) -> {
                    if (yaw != null) yaw.transform(bone, vehicle, state);
                    if (pitch != null) pitch.transform(bone, vehicle, state);
                };
            }
            default -> super.collectTransform(boneName);
        };
    }
}
