package frontline.combat.fcp.compat.vpb;

import com.atsuishio.superbwarfare.tools.ParticleTool;

import javax.annotation.Nullable;

public final class WarheadStats {
    public final float directDamage;
    public final float explosionDamage;
    public final float explosionRadius;
    @Nullable
    public final ParticleTool.ParticleType explosionParticle;
    public final int fireTime;
    public final boolean destroyBlocks;

    public WarheadStats(float directDamage, float explosionDamage, float explosionRadius,
                        @Nullable ParticleTool.ParticleType explosionParticle,
                        int fireTime, boolean destroyBlocks) {
        this.directDamage = directDamage;
        this.explosionDamage = explosionDamage;
        this.explosionRadius = explosionRadius;
        this.explosionParticle = explosionParticle;
        this.fireTime = fireTime;
        this.destroyBlocks = destroyBlocks;
    }


    public boolean hasExplosion() {
        return explosionDamage > 0f && explosionRadius > 0f;
    }


    public boolean hasDirectHit() {
        return directDamage > 0f;
    }


    public ParticleTool.ParticleType resolveExplosionParticle() {
        return explosionParticle != null ? explosionParticle : particleTypeForRadius(explosionRadius);
    }


    public static ParticleTool.ParticleType particleTypeForRadius(float radius) {
        if (radius < 2.0f) return ParticleTool.ParticleType.MINI;
        if (radius < 4.0f) return ParticleTool.ParticleType.SMALL;
        if (radius < 7.0f) return ParticleTool.ParticleType.MEDIUM;
        if (radius < 10.0f) return ParticleTool.ParticleType.LARGE;
        if (radius < 20.0f) return ParticleTool.ParticleType.HUGE;
        return ParticleTool.ParticleType.GIANT;
    }

    @Override
    public String toString() {
        return "WarheadStats{direct=" + directDamage + ", explosion=" + explosionDamage
                + ", radius=" + explosionRadius + ", particle=" + (explosionParticle != null ? explosionParticle : "auto")
                + ", fireTime=" + fireTime + ", destroyBlocks=" + destroyBlocks + '}';
    }
}
