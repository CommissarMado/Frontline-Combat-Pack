package frontline.combat.fcp.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** A blade of cut grass or a leaf thrown up by a rotor: light, so it spins and drifts down slowly. Tinted by HelicopterDownwash. */
public class GrassClippingParticle extends TextureSheetParticle {

    private final float spin, swayPhase;

    protected GrassClippingParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = 0.2f + this.random.nextFloat() * 0.12f;
        this.lifetime = 40 + this.random.nextInt(20);
        this.gravity = 0.025f;
        this.friction = 0.96f;
        this.spin = (this.random.nextFloat() - 0.5f) * 0.5f;
        this.swayPhase = this.random.nextFloat() * (float) Math.PI * 2;
        this.roll = this.oRoll = this.random.nextFloat() * (float) Math.PI * 2;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        this.roll += this.spin;
        this.xd += Mth.sin(this.age * 0.3f + this.swayPhase) * 0.006f;
        this.zd += Mth.cos(this.age * 0.25f + this.swayPhase) * 0.006f;
        super.tick();
        this.alpha = Math.min((this.lifetime - this.age) / 10f, 1f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new GrassClippingParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
