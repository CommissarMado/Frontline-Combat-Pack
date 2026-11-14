package frontline.combat.fcp.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.particle.FCPMuzzleParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.PARTICLE_TYPE, FCP.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<FCPMuzzleParticleOption>> MUZZLE_SMOKE =
            PARTICLE_TYPES.register("muzzle_smoke", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    public static final DeferredHolder<ParticleType<?>, ParticleType<FCPMuzzleParticleOption>> MUZZLE_BLOOM =
            PARTICLE_TYPES.register("muzzle_bloom", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    public static final DeferredHolder<ParticleType<?>, ParticleType<FCPMuzzleParticleOption>> MUZZLE_FLASH =
            PARTICLE_TYPES.register("muzzle_flash", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    public static final DeferredHolder<ParticleType<?>, ParticleType<FCPMuzzleParticleOption>> MUZZLE_BANG =
            PARTICLE_TYPES.register("muzzle_bang", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    public static final DeferredHolder<ParticleType<?>, ParticleType<FCPMuzzleParticleOption>> MUZZLE_SPARK =
            PARTICLE_TYPES.register("muzzle_spark", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DOWNWASH_DUST =
            PARTICLE_TYPES.register("downwash_dust", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GRASS_CLIPPING =
            PARTICLE_TYPES.register("grass_clipping", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LEAF_FLECK =
            PARTICLE_TYPES.register("leaf_fleck", () -> new SimpleParticleType(true));

    private static <T extends ParticleOptions> ParticleType<T> create(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return new ParticleType<>(true) {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodec;
            }
        };
    }
}
