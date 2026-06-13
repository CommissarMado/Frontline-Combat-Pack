package frontline.combat.fcp.init;

import com.mojang.serialization.Codec;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.particle.FCPMuzzleParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, FCP.MODID);

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_SMOKE =
            PARTICLE_TYPES.register("muzzle_smoke", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.DESERIALIZER));

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_BLOOM =
            PARTICLE_TYPES.register("muzzle_bloom", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.DESERIALIZER));

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_FLASH =
            PARTICLE_TYPES.register("muzzle_flash", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.DESERIALIZER));

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_BANG =
            PARTICLE_TYPES.register("muzzle_bang", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.DESERIALIZER));

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_SPARK =
            PARTICLE_TYPES.register("muzzle_spark", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.DESERIALIZER));

    @SuppressWarnings("deprecation")
    private static <T extends ParticleOptions> ParticleType<T> create(Codec<T> codec, ParticleOptions.Deserializer<T> deserializer) {
        return new ParticleType<>(true, deserializer) {
            @Override
            public Codec<T> codec() {
                return codec;
            }
        };
    }
}