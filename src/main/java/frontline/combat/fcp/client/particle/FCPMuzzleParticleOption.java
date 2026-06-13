package frontline.combat.fcp.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frontline.combat.fcp.init.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

public record FCPMuzzleParticleOption (
        int color,
        int life,
        float fade,
        int animationSpeed,
        float baseScale,
        float targetScale,
        int frameCount,
        int layer,
        boolean lingerSmoke,
        int movementDuration,
        int attachVehicleId,
        int attachSeatIndex
) implements ParticleOptions {

    public static final int NO_ATTACH = -1;

    public static final int LAYER_SMOKE = 0;
    public static final int LAYER_BANG_STATIC = 1;
    public static final int LAYER_BANG_SPARK = 2;
    public static final int LAYER_BLOOM = 3;

    public FCPMuzzleParticleOption(float r, float g, float b, int life, float fade, int animationSpeed, float baseScale, float targetScale, int frameCount, int layer) {
        this(r, g, b, life, fade, animationSpeed, baseScale, targetScale, frameCount, layer, false, 0);
    }

    public FCPMuzzleParticleOption(float r, float g, float b, int life, float fade, int animationSpeed, float baseScale, float targetScale, int frameCount, int layer, boolean lingerSmoke) {
        this(r, g, b, life, fade, animationSpeed, baseScale, targetScale, frameCount, layer, lingerSmoke, 0);
    }

    public FCPMuzzleParticleOption(float r, float g, float b, int life, float fade, int animationSpeed, float baseScale, float targetScale, int frameCount, int layer, boolean lingerSmoke, int movementDuration) {
        this(
                ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255),
                life,
                fade,
                animationSpeed,
                baseScale,
                targetScale,
                frameCount,
                layer,
                lingerSmoke,
                movementDuration,
                NO_ATTACH,
                0
        );
    }

    public boolean hasBarrelAttach() {
        return attachVehicleId >= 0 && layer == LAYER_SMOKE && lingerSmoke;
    }

    public FCPMuzzleParticleOption withBarrelAttach(int vehicleId, int seatIndex) {
        return new FCPMuzzleParticleOption(
                color, life, fade, animationSpeed, baseScale, targetScale, frameCount, layer,
                lingerSmoke, movementDuration, vehicleId, seatIndex
        );
    }

    public float red() {
        return ((color >> 16) & 255) / 255f;
    }

    public float green() {
        return ((color >> 8) & 255) / 255f;
    }

    public float blue() {
        return (color & 255) / 255f;
    }

    @Override
    public ParticleType<?> getType() {
        return switch (layer) {
            case LAYER_BLOOM -> ModParticleTypes.MUZZLE_BLOOM.get();
            case LAYER_BANG_STATIC, LAYER_BANG_SPARK -> ModParticleTypes.MUZZLE_BANG.get();
            default -> ModParticleTypes.MUZZLE_SMOKE.get();
        };
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeInt(color);
        buffer.writeInt(life);
        buffer.writeFloat(fade);
        buffer.writeInt(animationSpeed);
        buffer.writeFloat(baseScale);
        buffer.writeFloat(targetScale);
        buffer.writeInt(frameCount);
        buffer.writeInt(layer);
        buffer.writeBoolean(lingerSmoke);
        buffer.writeVarInt(movementDuration);
        buffer.writeVarInt(attachVehicleId);
        buffer.writeVarInt(attachSeatIndex);
    }

    @Override
    public String writeToString() {
        return ForgeRegistries.PARTICLE_TYPES.getKey(getType()) + " [" + color + ", " + life + ", " + fade + ", " + animationSpeed + ", " + baseScale + ", " + targetScale + ", " + frameCount + ", " + layer + ", " + lingerSmoke + ", " + movementDuration + ", " + attachVehicleId + ", " + attachSeatIndex + "]";
    }

    public static final Codec<FCPMuzzleParticleOption> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("color").forGetter(FCPMuzzleParticleOption::color),
            Codec.INT.fieldOf("life").forGetter(FCPMuzzleParticleOption::life),
            Codec.FLOAT.fieldOf("fade").forGetter(FCPMuzzleParticleOption::fade),
            Codec.INT.fieldOf("animationSpeed").forGetter(FCPMuzzleParticleOption::animationSpeed),
            Codec.FLOAT.fieldOf("baseScale").forGetter(FCPMuzzleParticleOption::baseScale),
            Codec.FLOAT.fieldOf("targetScale").forGetter(FCPMuzzleParticleOption::targetScale),
            Codec.INT.fieldOf("frameCount").forGetter(FCPMuzzleParticleOption::frameCount),
            Codec.INT.fieldOf("layer").forGetter(FCPMuzzleParticleOption::layer),
            Codec.BOOL.fieldOf("lingerSmoke").forGetter(FCPMuzzleParticleOption::lingerSmoke),
            Codec.INT.optionalFieldOf("movementDuration", 0).forGetter(FCPMuzzleParticleOption::movementDuration),
            Codec.INT.optionalFieldOf("attachVehicleId", NO_ATTACH).forGetter(FCPMuzzleParticleOption::attachVehicleId),
            Codec.INT.optionalFieldOf("attachSeatIndex", 0).forGetter(FCPMuzzleParticleOption::attachSeatIndex)
    ).apply(builder, FCPMuzzleParticleOption::new));

    @SuppressWarnings("deprecation")
    public static final Deserializer<FCPMuzzleParticleOption> DESERIALIZER = new Deserializer<>() {
        @Override
        public FCPMuzzleParticleOption fromCommand(ParticleType<FCPMuzzleParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int color = reader.readInt();
            reader.expect(' ');
            int life = reader.readInt();
            reader.expect(' ');
            float fade = reader.readFloat();
            reader.expect(' ');
            int animationSpeed = reader.readInt();
            reader.expect(' ');
            float baseScale = reader.readFloat();
            reader.expect(' ');
            float targetScale = reader.readFloat();
            reader.expect(' ');
            int frameCount = reader.readInt();
            reader.expect(' ');
            int layer = reader.readInt();
            reader.expect(' ');
            boolean lingerSmoke = reader.readBoolean();
            reader.expect(' ');
            int movementDuration = reader.readInt();
            reader.expect(' ');
            int attachVehicleId = reader.readInt();
            reader.expect(' ');
            int attachSeatIndex = reader.readInt();
            return new FCPMuzzleParticleOption(color, life, fade, animationSpeed, baseScale, targetScale, frameCount, layer, lingerSmoke, movementDuration, attachVehicleId, attachSeatIndex);
        }

        @Override
        public FCPMuzzleParticleOption fromNetwork(ParticleType<FCPMuzzleParticleOption> type, FriendlyByteBuf buffer) {
            return new FCPMuzzleParticleOption(
                    buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readInt(),
                    buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readInt(), buffer.readBoolean(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()
            );
        }
    };
}