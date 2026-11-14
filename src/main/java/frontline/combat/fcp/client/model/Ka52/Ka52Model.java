package frontline.combat.fcp.client.model.Ka52;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Ka52.Ka52Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Ka-52. Coaxial rotors: "rotor1" clockwise and "rotor2" counter-clockwise seen from above. The landing gear folds by
 * the user's Blockbench rotations as they are (left: Y +10, X -70; right: Y -10, X -70; nose: X -55) and their
 * position moves negated (left pos X +10 -> -10, right -10 -> +10: GeckoLib draws position X mirrored), so the legs
 * tuck in rather than out (per the user's in-game check). The 2A42 ("barrel2") follows the co-pilot's camera
 * turret within its own mount limits (Ka52Entity), "GUN" recoils straight back. Missiles hide as they're fired, in
 * Ka52Gen's firing order.
 */
public class Ka52Model extends VehicleModel<Ka52Entity> {

    /** Firing order per weapon (Ka52Gen): the k-th missile fired hides once ammo <= magazine - 1 - k. */
    private static final List<String> PILOT_ATGM = List.of("pilotatgm3", "pilotatgm", "pilotatgm4", "pilotatgm2");
    private static final List<String> COPILOT_ATGM = List.of("copilotatgm4", "copilotatgm2", "copilotatgm5", "copilotatgm", "pexot_raket4", "copilotatgm3");
    private static final List<String> IGLA = List.of("igla", "igla2");

    @Override
    public ResourceLocation getModelResource(Ka52Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(FCP.MODID, "geo/ka52.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Ka52Entity> collectTransform(String boneName) {
        TransformContext<Ka52Entity> missile = missile(boneName, "ATGM", PILOT_ATGM);
        if (missile == null) missile = missile(boneName, "ATGM-Wire", COPILOT_ATGM);
        if (missile == null) missile = missile(boneName, "Igla", IGLA);
        if (missile != null) return missile;

        return switch (boneName) {
            case "rotor1" -> (bone, v, s) -> bone.setRotY(-Mth.lerp(s.getPartialTick(), v.getPropellerRotO(), v.getPropellerRot()));
            case "rotor2" -> (bone, v, s) -> bone.setRotY(Mth.lerp(s.getPartialTick(), v.getPropellerRotO(), v.getPropellerRot()));
            case "gearleft" -> (bone, v, s) -> {
                float g = v.gear(s.getPartialTick());
                bone.setRotY(10 * g * Mth.DEG_TO_RAD);
                bone.setRotX(-70 * g * Mth.DEG_TO_RAD);
                bone.setPosX(-10 * g);
            };
            case "gearright" -> (bone, v, s) -> {
                float g = v.gear(s.getPartialTick());
                bone.setRotY(-10 * g * Mth.DEG_TO_RAD);
                bone.setRotX(-70 * g * Mth.DEG_TO_RAD);
                bone.setPosX(10 * g);
            };
            case "gearfront" -> (bone, v, s) -> bone.setRotX(-55 * v.gear(s.getPartialTick()) * Mth.DEG_TO_RAD);
            // The cannon's clamped angles, mapped to the bone like SBW maps the camera turret's ("turret" yaw, "barrel" -pitch).
            case "barrel2" -> (bone, v, s) -> {
                bone.setRotY(v.cannonYaw(s.getPartialTick()) * Mth.DEG_TO_RAD);
                bone.setRotX(-v.cannonPitch(s.getPartialTick()) * Mth.DEG_TO_RAD);
            };
            case "GUN" -> (bone, v, s) -> {
                ModelBoneTransforms.clearRecoilOffsets(bone);
                if (v.getCannonRecoilTime() <= 0 || !Ka52Entity.CANNON.equals(CannonRecoilTransforms.gunnerGunName(v))) return;
                CannonRecoilTransforms.apply(bone, v, CannonRecoilTransforms.Profile.ABRAMS);
            };
            default -> super.collectTransform(boneName);
        };
    }

    private static @Nullable TransformContext<Ka52Entity> missile(String boneName, String weapon, List<String> order) {
        int k = order.indexOf(boneName);
        if (k < 0) return null;
        int count = order.size() - 1 - k;
        return (bone, v, s) -> bone.setHidden(v.fired(weapon, count));
    }
}
