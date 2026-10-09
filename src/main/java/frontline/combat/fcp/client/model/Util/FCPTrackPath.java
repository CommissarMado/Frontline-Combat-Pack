package frontline.combat.fcp.client.model.Util;

import net.minecraft.util.Mth;

public final class FCPTrackPath {

    private static final int ROT_X = 0;
    private static final int MOVE_Y = 1;
    private static final int MOVE_Z = 2;

    private final float[][] keyframes;
    private final int trackCount;
    private final int animationLength;
    private final int maxIdx;
    private final float startY;
    private final float startZ;
    private final boolean reversed;
    private final float rotationScale;
    private final float rotationOffset;

    public FCPTrackPath(int trackCount, int animationLength, float[][] keyframes) {
        this(keyframes, trackCount, animationLength, false, 1f, 0f);
    }

    private FCPTrackPath(float[][] keyframes, int trackCount, int animationLength, boolean reversed, float rotationScale, float rotationOffset) {
        if (keyframes.length < 2) {
            throw new IllegalArgumentException("A track path needs at least 2 keyframes");
        }
        if (trackCount <= 0 || animationLength <= 0) {
            throw new IllegalArgumentException("trackCount and animationLength must be positive");
        }
        this.keyframes = keyframes;
        this.trackCount = trackCount;
        this.animationLength = animationLength;
        this.maxIdx = keyframes.length - 1;
        this.startY = keyframes[0][MOVE_Y];
        this.startZ = keyframes[0][MOVE_Z];
        this.reversed = reversed;
        this.rotationScale = rotationScale;
        this.rotationOffset = rotationOffset;
    }

    public FCPTrackPath reversed() {
        return new FCPTrackPath(keyframes, trackCount, animationLength, !reversed, rotationScale, rotationOffset);
    }

    public FCPTrackPath rotationInverted() {
        return new FCPTrackPath(keyframes, trackCount, animationLength, reversed, -rotationScale, rotationOffset);
    }

    public FCPTrackPath rotationOffset(float degrees) {
        return new FCPTrackPath(keyframes, trackCount, animationLength, reversed, rotationScale, rotationOffset + degrees);
    }

    public FCPTrackPath animationLength(int length) {
        return new FCPTrackPath(keyframes, trackCount, length, reversed, rotationScale, rotationOffset);
    }

    public float rotX(float t) {
        return rotationScale * sample(t, ROT_X) + rotationOffset;
    }

    public float moveY(float t) {
        return sample(t, MOVE_Y) - startY;
    }

    public float moveZ(float t) {
        return sample(t, MOVE_Z) - startZ;
    }

    public float trackDistance() {
        return (float) animationLength / trackCount;
    }

    public int trackCount() {
        return trackCount;
    }

    public int animationLength() {
        return animationLength;
    }

    private float sample(float t, int component) {
        float wrapped = t % animationLength;
        if (wrapped < 0) {
            wrapped += animationLength;
        }
        if (reversed) {
            wrapped = animationLength - wrapped;
            if (wrapped >= animationLength) {
                wrapped -= animationLength;
            }
        }
        float normalized = (wrapped / animationLength) * maxIdx;
        int idx1 = Mth.clamp((int) normalized, 0, maxIdx - 1);
        float frac = normalized - idx1;

        float p1 = keyframes[idx1][component];
        float p2 = keyframes[idx1 + 1][component];

        if (component == ROT_X) {
            // Take the short way round, so tables that wrap at +-180 interpolate correctly
            float diff = p2 - p1;
            if (diff > 180f) {
                p2 -= 360f;
            } else if (diff < -180f) {
                p2 += 360f;
            }
        }

        return Mth.lerp(frac, p1, p2);
    }
}
