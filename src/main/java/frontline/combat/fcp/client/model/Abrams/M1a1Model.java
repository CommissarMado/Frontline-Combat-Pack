package frontline.combat.fcp.client.model.Abrams;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class M1a1Model extends VehicleModel<M1a1Entity> {

    @Override
    public ResourceLocation getModelResource(M1a1Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/m1a1.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    private static final int TRACK_COUNT = 77;
    // The entity keeps SBW's default getTrackAnimationLength() of 100 - no entity changes needed.
    private static final int TRACK_LENGTH = 100;
    private static final float TRACK_DISTANCE = (float) TRACK_LENGTH / TRACK_COUNT;
    private static final int MAX_IDX = 77; // 1 keyframes per link; last row closes the loop

    // {rotX, y, z} - rotation runs continuously through one full turn, so no +-180 correction is needed
    private static final float[][] KEYFRAMES = {
            {-2.84f, 23.76f, 64.24f},
            {2.39f, 23.59f, 60.26f},
            {2.29f, 23.42f, 56.28f},
            {2.18f, 23.27f, 52.30f},
            {2.08f, 23.12f, 48.32f},
            {1.97f, 22.98f, 44.35f},
            {1.87f, 22.85f, 40.37f},
            {1.76f, 22.72f, 36.39f},
            {1.66f, 22.60f, 32.41f},
            {1.55f, 22.49f, 28.43f},
            {1.45f, 22.38f, 24.45f},
            {1.34f, 22.29f, 20.47f},
            {1.24f, 22.20f, 16.49f},
            {1.13f, 22.11f, 12.51f},
            {1.03f, 22.04f, 8.53f},
            {0.92f, 21.97f, 4.55f},
            {0.82f, 21.91f, 0.57f},
            {0.71f, 21.86f, -3.41f},
            {0.61f, 21.81f, -7.40f},
            {0.50f, 21.77f, -11.38f},
            {0.40f, 21.74f, -15.36f},
            {0.29f, 21.72f, -19.34f},
            {0.19f, 21.70f, -23.32f},
            {0.08f, 21.69f, -27.30f},
            {-0.02f, 21.69f, -31.28f},
            {-0.13f, 21.69f, -35.26f},
            {-0.23f, 21.71f, -39.24f},
            {-0.34f, 21.73f, -43.23f},
            {-0.44f, 21.75f, -47.21f},
            {-0.55f, 21.79f, -51.19f},
            {-0.65f, 21.83f, -55.17f},
            {-0.76f, 21.88f, -59.15f},
            {-0.86f, 21.93f, -63.13f},
            {2.76f, 22.00f, -67.11f},
            {37.30f, 20.88f, -70.84f},
            {78.80f, 17.57f, -72.91f},
            {120.26f, 13.74f, -72.26f},
            {150.20f, 11.20f, -69.27f},
            {151.59f, 9.31f, -65.77f},
            {151.59f, 7.42f, -62.27f},
            {151.59f, 5.52f, -58.77f},
            {151.59f, 3.63f, -55.26f},
            {164.09f, 1.84f, -51.71f},
            {180.00f, 1.61f, -47.76f},
            {180.00f, 1.61f, -43.77f},
            {180.00f, 1.61f, -39.79f},
            {180.00f, 1.61f, -35.81f},
            {180.00f, 1.61f, -31.83f},
            {180.00f, 1.61f, -27.85f},
            {180.00f, 1.61f, -23.87f},
            {180.00f, 1.61f, -19.89f},
            {180.00f, 1.61f, -15.91f},
            {180.00f, 1.61f, -11.92f},
            {180.00f, 1.61f, -7.94f},
            {180.00f, 1.61f, -3.96f},
            {180.00f, 1.61f, 0.02f},
            {180.00f, 1.61f, 4.00f},
            {180.00f, 1.61f, 7.98f},
            {180.00f, 1.61f, 11.96f},
            {180.00f, 1.61f, 15.94f},
            {180.00f, 1.61f, 19.92f},
            {180.00f, 1.61f, 23.91f},
            {180.00f, 1.61f, 27.89f},
            {180.00f, 1.61f, 31.87f},
            {180.00f, 1.61f, 35.85f},
            {180.00f, 1.61f, 39.83f},
            {180.00f, 1.61f, 43.81f},
            {180.00f, 1.61f, 47.79f},
            {191.05f, 1.68f, 51.77f},
            {213.43f, 3.45f, 55.30f},
            {213.62f, 5.65f, 58.61f},
            {213.62f, 7.86f, 61.93f},
            {213.62f, 10.06f, 65.24f},
            {222.02f, 12.31f, 68.53f},
            {255.46f, 15.62f, 70.61f},
            {290.59f, 19.54f, 70.40f},
            {325.67f, 22.62f, 67.99f},
            {357.16f, 23.76f, 64.24f}
    };

    private static final float START_Y = 23.76f;
    private static final float START_Z = 64.24f;

    private float getKeyframeValue(float t, int component) {
        float wrapped = t % TRACK_LENGTH;
        if (wrapped < 0) {
            wrapped += TRACK_LENGTH;
        }
        float normalized = (wrapped / TRACK_LENGTH) * MAX_IDX;
        int idx1 = Mth.clamp((int) normalized, 0, MAX_IDX - 1);
        float frac = normalized - idx1;
        return Mth.lerp(frac, KEYFRAMES[idx1][component], KEYFRAMES[idx1 + 1][component]);
    }

    @Override
    public float getBoneRotX(float t) {
        return getKeyframeValue(t, 0);
    }

    @Override
    public float getBoneMoveY(float t) {
        return getKeyframeValue(t, 1) - START_Y;
    }

    @Override
    public float getBoneMoveZ(float t) {
        return getKeyframeValue(t, 2) - START_Z;
    }

    @Override
    public float getTrackDistance() {
        return TRACK_DISTANCE;
    }
}
