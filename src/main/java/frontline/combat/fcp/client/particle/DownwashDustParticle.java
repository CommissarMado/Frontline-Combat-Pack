package frontline.combat.fcp.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;

/** The veil under a helicopter: a large, transparent puff, tinted, sized and thickened by HelicopterDownwash. */
public class DownwashDustParticle extends TextureSheetParticle {

    private static final int MAX_LIGHT_SEARCH = 12;

    private float peakAlpha = 0.14f;
    private int light;

    protected DownwashDustParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = 1f;
        // Thrown out hard along the ground, braking quickly, with a slight rise as it loses speed.
        this.lifetime = 30 + this.random.nextInt(20);
        this.hasPhysics = false;
        this.gravity = -0.015f;
        this.friction = 0.92f;
        this.alpha = 0f;
        this.roll = this.oRoll = this.random.nextFloat() * (float) Math.PI * 2;
        this.pickSprite(sprites);
        this.light = openAirLight();
    }

    /** Puffs overlap heavily, so each one is faint. */
    public void setPeakAlpha(float peakAlpha) {
        this.peakAlpha = peakAlpha;
    }

    @Override
    public void tick() {
        super.tick();
        float life = (float) this.age / this.lifetime;
        this.alpha = this.peakAlpha * Math.min(life / 0.15f, 1f) * Math.min((1f - life) / 0.65f, 1f);
        this.quadSize *= 1.012f;
        if (this.age % 10 == 0) this.light = openAirLight();
    }

    /** A puff's centre can drift into a hillside, where the light is 0 and the whole puff would turn black: light it from the air above. */
    private int openAirLight() {
        BlockPos.MutableBlockPos pos = BlockPos.containing(this.x, this.y, this.z).mutable();
        for (int up = 0; up < MAX_LIGHT_SEARCH && this.level.getBlockState(pos).isSolidRender(this.level, pos); up++) pos.move(Direction.UP);
        return LevelRenderer.getLightColor(this.level, pos);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return this.light;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FCPParticleRenderTypes.SOFT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new DownwashDustParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
