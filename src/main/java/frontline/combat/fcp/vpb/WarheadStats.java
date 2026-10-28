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
    /** Squad damage type for vehicle armour (heat, hat, explosive, frag, kinetic), or null to leave SBW's modifiers. */
    @Nullable
    public final String type;
    /** Armour penetration in mm, checked against the face of the vehicle that was hit. */
    public final float penetration;
    /** Share of the crater radius kept (fragmentation rounds dig less); negative = the type's default. */
    public final float blockDamage;

    public WarheadStats(float directDamage, float explosionDamage, float explosionRadius,
                        @Nullable ParticleTool.ParticleType explosionParticle,
                        int fireTime, boolean destroyBlocks,
                        @Nullable String type, float penetration, float blockDamage) {
        this.directDamage = directDamage;
        this.explosionDamage = explosionDamage;
        this.explosionRadius = explosionRadius;
        this.explosionParticle = explosionParticle;
        this.fireTime = fireTime;
        this.destroyBlocks = destroyBlocks;
        this.type = type;
        this.penetration = penetration;
        this.blockDamage = blockDamage;
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
                + ", fireTime=" + fireTime + ", destroyBlocks=" + destroyBlocks
                + ", type=" + type + ", pen=" + penetration + ", blockDamage=" + blockDamage + '}';
    }
}
