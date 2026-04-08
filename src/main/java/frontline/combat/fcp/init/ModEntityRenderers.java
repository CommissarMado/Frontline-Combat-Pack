package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.renderer.Bmp.BMP1Renderer;
import frontline.combat.fcp.client.renderer.Bmp.BMP1URenderer;
import frontline.combat.fcp.client.renderer.Bmp.BMP2Renderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrGLRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrMGRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrRWSRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrRenderer;
import frontline.combat.fcp.client.renderer.Huey.HueyRenderer;
import frontline.combat.fcp.client.renderer.Kamaz.KamazRenderer;
import frontline.combat.fcp.client.renderer.Lav.Lav25Renderer;
import frontline.combat.fcp.client.renderer.Littlebird.LittlebirdArmedRenderer;
import frontline.combat.fcp.client.renderer.Littlebird.LittlebirdRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Hellfire.WireGuidedHellfireRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Malyutka.MalyutkaRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Sidewinder.SidewinderRenderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerM2Renderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerMGSRenderer;
import frontline.combat.fcp.client.renderer.T72av.T72AVRenderer;
import frontline.combat.fcp.client.renderer.T80bvm.T80BVMRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxBMPRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxRocketPodRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxSpg9Renderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZDSHKARenderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZRenderer;
import frontline.combat.fcp.client.renderer.Ural.UralGradRenderer;
import frontline.combat.fcp.client.renderer.Ural.UralRenderer;
import frontline.combat.fcp.client.renderer.Viper.ViperRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Hellfire.LockOnHellfireRenderer;
import frontline.combat.fcp.entity.vehicle.T80bvm.T80BVMEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid = FCP.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX.get(), ToyotaHiluxRenderer::new);
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX_ROCKET_POD.get(), ToyotaHiluxRocketPodRenderer::new);
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX_BMP.get(), ToyotaHiluxBMPRenderer::new);
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX_SPG9.get(), ToyotaHiluxSpg9Renderer::new);

        event.registerEntityRenderer(ModEntities.UAZ.get(), UAZRenderer::new);
        event.registerEntityRenderer(ModEntities.UAZ_DSHKA.get(), UAZDSHKARenderer::new);

        event.registerEntityRenderer(ModEntities.STRYKER_MGS.get(), StrykerMGSRenderer::new);
        event.registerEntityRenderer(ModEntities.STRYKER_M2.get(), StrykerM2Renderer::new);

        event.registerEntityRenderer(ModEntities.LITTLEBIRD.get(), LittlebirdRenderer::new);
        event.registerEntityRenderer(ModEntities.LITTLEBIRD_ARMED.get(), LittlebirdArmedRenderer::new);

        event.registerEntityRenderer(ModEntities.BMP1.get(), BMP1Renderer::new);
        event.registerEntityRenderer(ModEntities.BMP1U.get(), BMP1URenderer::new);

        event.registerEntityRenderer(ModEntities.BMP2.get(), BMP2Renderer::new);

        event.registerEntityRenderer(ModEntities.LAV25.get(), Lav25Renderer::new);

        event.registerEntityRenderer(ModEntities.T72AV.get(), T72AVRenderer::new);
        event.registerEntityRenderer(ModEntities.T80BVM.get(), T80BVMRenderer::new);

        event.registerEntityRenderer(ModEntities.URAL.get(), UralRenderer::new);
        event.registerEntityRenderer(ModEntities.URAL_GRAD.get(), UralGradRenderer::new);

        event.registerEntityRenderer(ModEntities.KAMAZ.get(), KamazRenderer::new);

        event.registerEntityRenderer(ModEntities.VIPER.get(), ViperRenderer::new);

        event.registerEntityRenderer(ModEntities.GAZ_TIGR.get(), GazTigrRenderer::new);
        event.registerEntityRenderer(ModEntities.GAZ_TIGR_RWS.get(), GazTigrRWSRenderer::new);
        event.registerEntityRenderer(ModEntities.GAZ_TIGR_MG.get(), GazTigrMGRenderer::new);
        event.registerEntityRenderer(ModEntities.GAZ_TIGR_GL.get(), GazTigrGLRenderer::new);

        event.registerEntityRenderer(ModEntities.HUEY.get(), HueyRenderer::new);

        // Projectiles
        event.registerEntityRenderer(ModEntities.LOCK_ON_HELLFIRE.get(), LockOnHellfireRenderer::new);
        event.registerEntityRenderer(ModEntities.WIRE_GUIDED_HELLFIRE.get(), WireGuidedHellfireRenderer::new);

        event.registerEntityRenderer(ModEntities.SIDEWINDER.get(), SidewinderRenderer::new);

        event.registerEntityRenderer(ModEntities.MALYUTKA.get(), MalyutkaRenderer::new);
    }
}
