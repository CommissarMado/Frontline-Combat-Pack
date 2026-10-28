package frontline.combat.fcp.client.model.Puma;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Abrams.M1a1TrackPaths;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.client.sound.PumaLauncherLids;
import frontline.combat.fcp.entity.vehicle.Puma.PumaEntity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * Both Puma variants. No track or wheel animation: SBW would spin any "wheelL../wheelR.." bone, so
 * those are opted out. The ATGM variant's launcher ("ATGM") elevates with the gun about its own pivot,
 * its covers ("lid", "lid2") open when the gunner selects the ATGM (see PumaLauncherLids), and the
 * missiles ("rocket", "rocket2") disappear as they are fired. The commander's sight
 * ("weaponstationturret") turns with the commander's station but doesn't elevate.
 * <p>
 * Extends VehicleModel directly (like the Stryker TOW): FCPVehicleModel un-hides transform bones,
 * which would cancel the missile hide.
 */
public class PumaModel extends FCPVehicleModel<PumaEntity> {

    private static final String CANNON_WEAPON = "Cannon";
    /** Open lid angles in degrees (Blockbench X rotation). */
    private static final float LID_OPEN = -100f, LID2_OPEN = 100f;

    private static final FCPTrackPath TRACK_PATH = PumaTrackPaths.PUMA.reversed();

    protected FCPTrackPath getTrackPath() {
        return TRACK_PATH;
    }

    @Override
    public ResourceLocation getModelResource(PumaEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + HumveeAttachments.vehicleName(animatable.getType()) + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<PumaEntity> collectTransform(String boneName) {
        // Road wheels stay as modelled.
        if (boneName.startsWith("wheel")) return null;

        switch (boneName) {
            case "base":
                // SBW shakes "base" (the whole hull) on every recoiling shot; only gun2 recoils here.
                return null;
            case "ATGM":
                // Same elevation as the gun, about the launcher's own pivot.
                return super.collectTransform("barrel");
            case "weaponstationturret":
                // Commander's sight: traverses with the station, never elevates.
                return super.collectTransform("passengerWeaponStationYaw");
            case "lid":
                return (bone, vehicle, state) -> bone.setRotX(LID_OPEN * Mth.DEG_TO_RAD * PumaLauncherLids.openness(vehicle, state.getPartialTick()));
            case "lid2":
                return (bone, vehicle, state) -> bone.setRotX(LID2_OPEN * Mth.DEG_TO_RAD * PumaLauncherLids.openness(vehicle, state.getPartialTick()));
            case "rocket":
                // Magazine of 2: "rocket" goes with the first shot, "rocket2" with the second.
                return (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState(PumaEntity.ATGM_WEAPON, 1));
            case "rocket2":
                return (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState(PumaEntity.ATGM_WEAPON, 0));
            case "gun2":
                // Light recoil on the 30 mm, like the Dragoon.
                return (bone, vehicle, state) -> {
                    ModelBoneTransforms.clearRecoilOffsets(bone);
                    if (vehicle.getCannonRecoilTime() <= 0) return;
                    if (!CANNON_WEAPON.equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
                    CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.LIGHT);
                };
            default:
                return super.collectTransform(boneName);
        }
    }
}
