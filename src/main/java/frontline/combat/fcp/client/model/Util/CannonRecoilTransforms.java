package frontline.combat.fcp.client.model.Util;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;

public final class CannonRecoilTransforms {



    private static final float REFERENCE_TICKS = 42f;



    public enum Profile {

        STANDARD(12f, 3f),

        SIDETOSIDE(1f, 3f),

        LIGHT(3f, 0.8f);



        private final float slideMax;

        private final float kickMaxDeg;



        Profile(float slideMax, float kickMaxDeg) {

            this.slideMax = slideMax;

            this.kickMaxDeg = kickMaxDeg;

        }

    }



    private record RecoilOffsets(float slide, float kick) {

    }



    private CannonRecoilTransforms() {

    }



    @Nullable

    private static RecoilOffsets computeRecoilOffsets(VehicleEntity vehicle, Profile profile) {

        int recoilTime = vehicle.getCannonRecoilTime();

        if (recoilTime <= 0) {

            return null;

        }



        float force = Mth.clamp(vehicle.getCannonRecoilForce(), 0f, 2f);

        float progress = recoilTime / REFERENCE_TICKS;

        float slide = force * profile.slideMax * progress * progress;

        float kick = force * profile.kickMaxDeg * progress * progress

                * (float) Math.sin(0.2 * Math.PI * (recoilTime - 2.5))

                * Mth.DEG_TO_RAD;

        return new RecoilOffsets(slide, kick);

    }



    public static void apply(CoreGeoBone bone, VehicleEntity vehicle, Profile profile) {

        ModelBoneTransforms.clearRecoilOffsets(bone);

        RecoilOffsets recoil = computeRecoilOffsets(vehicle, profile);

        if (recoil == null) {

            return;

        }

        bone.setPosZ(recoil.slide);

        bone.setRotX(recoil.kick);

    }



    public static void applyBarrelPitchAndRecoil(

            CoreGeoBone bone,

            VehicleEntity vehicle,

            float modelTurretXRot,

            Profile profile

    ) {

        applyBarrelPitchAndRecoil(bone, vehicle, modelTurretXRot, profile, null);

    }



    public static void applyBarrelPitchAndRecoil(

            CoreGeoBone bone,

            VehicleEntity vehicle,

            float modelTurretXRot,

            Profile profile,

            @Nullable String requiredWeapon

    ) {

        float pitch = Mth.clamp(

                -modelTurretXRot,

                vehicle.getTurretMinPitch(),

                vehicle.getTurretMaxPitch()

        ) * Mth.DEG_TO_RAD;

        bone.setRotX(pitch);

        bone.setPosZ(0f);



        if (requiredWeapon != null && !requiredWeapon.equals(vehicle.getGunName(1))) {

            return;

        }



        RecoilOffsets recoil = computeRecoilOffsets(vehicle, profile);

        if (recoil == null) {

            return;

        }

        bone.setPosZ(recoil.slide);

        bone.setRotX(pitch + recoil.kick);

    }



    @Nullable

    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchBarrel(String boneName) {

        return matchBarrel(boneName, Profile.STANDARD);

    }



    @Nullable

    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchBarrel(String boneName, Profile profile) {

        return matchBarrelForWeapon(boneName, profile, null);

    }



    @Nullable

    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchBarrelForWeapon(

            String boneName,

            Profile profile,

            @Nullable String requiredWeapon

    ) {

        if (!"barrel".equals(boneName)) {

            return null;

        }

        return (geoBone, vehicle, state) -> {

            float turretXRot = Mth.lerp(state.getPartialTick(), vehicle.getTurretXRotO(), vehicle.getTurretXRot());

            applyBarrelPitchAndRecoil(geoBone, vehicle, turretXRot, profile, requiredWeapon);

        };

    }



    @Nullable

    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchBarrelForWeapon(

            String boneName,

            @Nullable String requiredWeapon

    ) {

        return matchBarrelForWeapon(boneName, Profile.STANDARD, requiredWeapon);

    }



    @Nullable

    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> match(String boneName, String bone) {

        return match(boneName, bone, Profile.STANDARD);

    }



    @Nullable

    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> match(String boneName, String bone, Profile profile) {

        if (!bone.equals(boneName)) {

            return null;

        }

        return (geoBone, vehicle, state) -> apply(geoBone, vehicle, profile);

    }



    @Nullable

    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> weaponRecoil(

            String boneName,

            String bone,

            String weaponName,

            Profile profile

    ) {

        if (!bone.equals(boneName)) {

            return null;

        }

        return (geoBone, vehicle, state) -> {

            ModelBoneTransforms.clearRecoilOffsets(geoBone);

            if (vehicle.getCannonRecoilTime() <= 0) {

                return;

            }

            if (!weaponName.equals(vehicle.getGunName(1))) {

                return;

            }

            apply(geoBone, vehicle, profile);

        };

    }



    @Nullable

    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> weaponRecoil(

            String boneName,

            String bone,

            String weaponName

    ) {

        return weaponRecoil(boneName, bone, weaponName, Profile.STANDARD);

    }

}