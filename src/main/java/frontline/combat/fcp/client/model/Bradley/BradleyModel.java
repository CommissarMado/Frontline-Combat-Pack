package frontline.combat.fcp.client.model.Bradley;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.client.sound.AtgmLauncherAnimation;
import frontline.combat.fcp.entity.vehicle.Bradley.BradleyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * All four Bradleys. No track or wheel animation. The 25 mm recoils straight back on "GUN". The TOW launcher ("BGM_71")
 * swings up a quarter turn about Z while its cover ("dort", a child of it) turns back the other way at the
 * same rate, driven by AtgmLauncherAnimation; the missiles ("BGM_2", "BGM_3") disappear as they are fired.
 * The A3s' Commander's Independent Viewer traverses on "weaponstationturret" and elevates on "weaponstationbarrel".
 * <p>
 * Extends VehicleModel directly (like the Puma): FCPVehicleModel un-hides transform bones, which would
 * cancel the missile hide.
 */
public class BradleyModel extends VehicleModel<BradleyEntity> {

    private static final String CANNON_WEAPON = "Cannon";
    /**
     * Raised launcher angle in GeckoLib's frame (degrees). GeckoLib mirrors X, so a Blockbench +90 on Z is
     * -90 here: this is the angle that brings the tubes up and outboard over the turret roof.
     */
    private static final float LAUNCHER_RAISED = -90f;

    @Override
    public ResourceLocation getModelResource(BradleyEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + animatable.variant() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<BradleyEntity> collectTransform(String boneName) {
        // Road wheels and track links stay as modelled.
        if (boneName.startsWith("WHE") || boneName.startsWith("GUS")) return null;

        switch (boneName) {
            case "base":
                // SBW shakes "base" on every recoiling shot; only the gun recoils here.
                return null;
            case "BGM_71":
                return (bone, vehicle, state) -> bone.setRotZ(LAUNCHER_RAISED * Mth.DEG_TO_RAD * AtgmLauncherAnimation.openness(vehicle, state.getPartialTick()));
            case "dort":
                return (bone, vehicle, state) -> bone.setRotZ(-LAUNCHER_RAISED * Mth.DEG_TO_RAD * AtgmLauncherAnimation.openness(vehicle, state.getPartialTick()));
            case "BGM_2":
                // Two in the launcher: "BGM_2" goes with the first shot, "BGM_3" with the second.
                return (bone, vehicle, state) -> bone.setHidden(vehicle.towAtMost(1));
            case "BGM_3":
                return (bone, vehicle, state) -> bone.setHidden(vehicle.towAtMost(0));
            case "weaponstationturret":
                return super.collectTransform("passengerWeaponStationYaw");
            case "weaponstationbarrel":
                return super.collectTransform("passengerWeaponStationPitch");
            case "GUN":
                return (bone, vehicle, state) -> {
                    ModelBoneTransforms.clearRecoilOffsets(bone);
                    if (vehicle.getCannonRecoilTime() <= 0) return;
                    if (!CANNON_WEAPON.equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
                    CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.FORWARDBACK);
                };
            default:
                return super.collectTransform(boneName);
        }
    }
}
