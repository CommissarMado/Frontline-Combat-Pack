package frontline.combat.fcp.client.model.Leopard2;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Leopard2.Leopard2Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * Leopard 2A4 / 2A6 / 2A7V; the geo is picked from the registry name. The geos were converted from the user's
 * bbmodels with the model turned to face SBW's front, so SBW's turret, barrel and wheel signs apply as they are.
 * <ul>
 *   <li>"camera": the commander's sight, on SBW's passenger weapon station - traverses with it, never elevates (the
 *       Puma's commander sight).</li>
 *   <li>"weaponstationturret" / "weaponstationbarrel": the loader's open-top MG3, aimed at the loader's look like the
 *       Abrams loader MG (Leopard2Entity.loaderAim).</li>
 *   <li>Road wheels, idlers and sprockets ("WHEL..."): SBW's left/right wheel spin by which side the bone is on. The
 *       tracks are rigid segments in these models and stay as modelled.</li>
 *   <li>"GUN" recoils with the 120 mm (the Abrams' straight travel).</li>
 * </ul>
 */
public class Leopard2Model extends FCPVehicleModel<Leopard2Entity> {

    @Override
    public ResourceLocation getModelResource(Leopard2Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + animatable.variantName() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Leopard2Entity> collectTransform(String boneName) {
        if (boneName.startsWith("WHEL")) {
            VehicleModel.TransformContext<Leopard2Entity> left = super.collectTransform("wheelL"), right = super.collectTransform("wheelR");
            if (left == null || right == null) return null;
            // GeckoLib negates the geo's x: a negative pivot x is the tank's left (+x in the vehicle JSON).
            return (bone, vehicle, state) -> (bone.getPivotX() < 0 ? left : right).transform(bone, vehicle, state);
        }
        return switch (boneName) {
            case "camera" -> super.collectTransform("passengerWeaponStationYaw");
            // Same sign conventions as SBW's passenger weapon station: yaw bone = -(look offset), pitch bone = -(look pitch).
            case "weaponstationturret" -> (bone, vehicle, state) -> bone.setRotY(-vehicle.loaderAim(state.getPartialTick())[0] * Mth.DEG_TO_RAD);
            case "weaponstationbarrel" -> (bone, vehicle, state) -> bone.setRotX(-vehicle.loaderAim(state.getPartialTick())[1] * Mth.DEG_TO_RAD);
            case "GUN" -> (bone, vehicle, state) -> {
                ModelBoneTransforms.clearRecoilOffsets(bone);
                if (vehicle.getCannonRecoilTime() <= 0 || !"Cannon".equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
                CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.ABRAMS);
            };
            default -> super.collectTransform(boneName);
        };
    }
}
