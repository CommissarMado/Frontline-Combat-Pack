package frontline.combat.fcp.firecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndirectFireBallisticsTest {

    @Test
    void sameHeightRangeMatchesProjectileFormula() {
        double velocity = 18.0;
        double gravity = 0.122097;
        double pitch = 35.0;

        double actual = IndirectFireBallistics.rangeAtPitch(
                velocity, gravity, 64.0, 64.0, pitch
        );
        double expected = velocity * velocity * Math.sin(Math.toRadians(pitch * 2.0)) / gravity;

        assertEquals(expected, actual, 1.0E-8);
    }

    @Test
    void targetAboveApexHasNoRangeSolution() {
        double actual = IndirectFireBallistics.rangeAtPitch(
                10.0, 0.05, 64.0, 2000.0, 45.0
        );

        assertEquals(0.0, actual);
    }

    @Test
    void zeroRadiusAlwaysTargetsBlockCenter() {
        BlockPos center = new BlockPos(-12, 70, 28);

        Vec3 sampled = IndirectFireBallistics.sampleTarget(center, 0, RandomSource.create(42L));

        assertEquals(center.getCenter(), sampled);
    }

    @Test
    void sampledTargetsStayInsideRadiusAndAreUniformByArea() {
        BlockPos center = new BlockPos(100, 72, -40);
        Vec3 centerPoint = center.getCenter();
        int radius = 30;
        RandomSource random = RandomSource.create(123456L);
        int samples = 10_000;
        double squaredRadiusSum = 0;

        for (int i = 0; i < samples; i++) {
            Vec3 sampled = IndirectFireBallistics.sampleTarget(center, radius, random);
            double dx = sampled.x - centerPoint.x;
            double dz = sampled.z - centerPoint.z;
            double squaredRadius = dx * dx + dz * dz;
            assertTrue(squaredRadius <= radius * radius + 1.0E-8);
            assertEquals(centerPoint.y, sampled.y, 0.0);
            squaredRadiusSum += squaredRadius;
        }

        double meanSquaredRadius = squaredRadiusSum / samples;
        double expectedMean = radius * radius / 2.0;
        assertEquals(expectedMean, meanSquaredRadius, expectedMean * 0.03);
    }
}
