package frontline.combat.fcp.client.particle;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = FCP.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModParticles {
    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.MUZZLE_SMOKE.get(), FCPMuzzleParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUZZLE_BLOOM.get(), FCPMuzzleParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUZZLE_FLASH.get(), FCPMuzzleParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUZZLE_BANG.get(), FCPMuzzleParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUZZLE_SPARK.get(), FCPMuzzleParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.DOWNWASH_DUST.get(), DownwashDustParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GRASS_CLIPPING.get(), GrassClippingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.LEAF_FLECK.get(), GrassClippingParticle.Provider::new);
    }
}
