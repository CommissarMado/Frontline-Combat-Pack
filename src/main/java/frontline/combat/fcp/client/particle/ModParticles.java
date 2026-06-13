package frontline.combat.fcp.client.particle;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FCP.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModParticles {
    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.MUZZLE_SMOKE.get(), FCPMuzzleParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUZZLE_BLOOM.get(), FCPMuzzleParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUZZLE_FLASH.get(), FCPMuzzleParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUZZLE_BANG.get(), FCPMuzzleParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUZZLE_SPARK.get(), FCPMuzzleParticle.Provider::new);
    }
}
