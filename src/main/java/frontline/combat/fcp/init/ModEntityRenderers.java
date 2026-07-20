package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.overlay.FcpPilotOverlay;
import frontline.combat.fcp.client.renderer.Aavp.AAVPRenderer;
import frontline.combat.fcp.client.renderer.Bmp1.BMP1AMRenderer;
import frontline.combat.fcp.client.renderer.Bmp1.BMP1Renderer;
import frontline.combat.fcp.client.renderer.Bmp1.BMP1URenderer;
import frontline.combat.fcp.client.renderer.Bmp2.BMP2Renderer;
import frontline.combat.fcp.client.renderer.Btr82.BTR82Renderer;
import frontline.combat.fcp.client.renderer.Fmtv.FMTVRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrGLRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrMGRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrRWSRenderer;
import frontline.combat.fcp.client.renderer.GazTigr.GazTigrRenderer;
import frontline.combat.fcp.client.renderer.Huey.HueyDoorGunnerM134Renderer;
import frontline.combat.fcp.client.renderer.Huey.HueyDoorGunnerM60Renderer;
import frontline.combat.fcp.client.renderer.Huey.HueyRenderer;
import frontline.combat.fcp.client.renderer.Huey.VenomRenderer;
import frontline.combat.fcp.client.renderer.Huey.HueyRocketsRenderer;
import frontline.combat.fcp.client.renderer.Humvee.HumveeRenderer;
import frontline.combat.fcp.client.renderer.Humvee.HumveeTOWRenderer;
import frontline.combat.fcp.client.renderer.JohnDeere.JohnDeereRenderer;
import frontline.combat.fcp.client.renderer.JohnDeere.SeederRenderer;
import frontline.combat.fcp.client.renderer.Kamaz.KamazRenderer;
import frontline.combat.fcp.client.renderer.Lav.Lav25Renderer;
import frontline.combat.fcp.client.renderer.Littlebird.LittlebirdArmedRenderer;
import frontline.combat.fcp.client.renderer.Littlebird.LittlebirdRenderer;
import frontline.combat.fcp.client.renderer.Matv.MATV9In1Renderer;
import frontline.combat.fcp.client.renderer.Matv.MATVCrowsRenderer;
import frontline.combat.fcp.client.renderer.Matv.MATVRenderer;
import frontline.combat.fcp.client.renderer.Matv.MATVTOWRenderer;
import frontline.combat.fcp.client.renderer.MemeVehicles.BigBirdRenderer;
import frontline.combat.fcp.client.renderer.MemeVehicles.LaHumveeRenderer;
import frontline.combat.fcp.client.renderer.MemeVehicles.WolfRenderer;
import frontline.combat.fcp.client.renderer.Mi17.MI17Renderer;
import frontline.combat.fcp.client.renderer.Novator.NovatorRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Hellfire.WireGuidedHellfireRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Malyutka.MalyutkaRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Sidewinder.SidewinderRenderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerM2Renderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerMGSRenderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerDragoonRenderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerMk19Renderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerTowRenderer;
import frontline.combat.fcp.client.renderer.Stryker.StrykerMortarRenderer;
import frontline.combat.fcp.client.renderer.T72av.T72AVRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxBMPRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxRocketPodRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxSpg9Renderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxMortarRenderer;
import frontline.combat.fcp.client.renderer.Toyota.ToyotaHiluxZu23Renderer;
import frontline.combat.fcp.client.renderer.Trailers.ExampleTrailer.ExampleTrailerRenderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZDSHKARenderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZSPG9Renderer;
import frontline.combat.fcp.client.renderer.Uaz.UAZRenderer;
import frontline.combat.fcp.client.renderer.Ural.UralGradRenderer;
import frontline.combat.fcp.client.renderer.Ural.UralRenderer;
import frontline.combat.fcp.client.renderer.Viper.ViperRenderer;
import frontline.combat.fcp.client.renderer.Projectile.Hellfire.LockOnHellfireRenderer;
import frontline.combat.fcp.entity.vehicle.JohnDeere.JohnDeereEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import frontline.combat.fcp.client.overlay.FcpDriverOverlay;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;

@Mod.EventBusSubscriber(modid = FCP.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX.get(), ToyotaHiluxRenderer::new);
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX_ROCKET_POD.get(), ToyotaHiluxRocketPodRenderer::new);
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX_BMP.get(), ToyotaHiluxBMPRenderer::new);
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX_SPG9.get(), ToyotaHiluxSpg9Renderer::new);
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX_MORTAR.get(), ToyotaHiluxMortarRenderer::new);
        event.registerEntityRenderer(ModEntities.TOYOTA_HILUX_ZU23.get(), ToyotaHiluxZu23Renderer::new);

        event.registerEntityRenderer(ModEntities.UAZ.get(), UAZRenderer::new);
        event.registerEntityRenderer(ModEntities.UAZ_DSHKA.get(), UAZDSHKARenderer::new);
        event.registerEntityRenderer(ModEntities.UAZ_SPG9.get(), UAZSPG9Renderer::new);

        event.registerEntityRenderer(ModEntities.STRYKER_MGS.get(), StrykerMGSRenderer::new);
        event.registerEntityRenderer(ModEntities.STRYKER_M2.get(), StrykerM2Renderer::new);
        event.registerEntityRenderer(ModEntities.STRYKER_DRAGOON.get(), StrykerDragoonRenderer::new);
        event.registerEntityRenderer(ModEntities.STRYKER_MK19.get(), StrykerMk19Renderer::new);
        event.registerEntityRenderer(ModEntities.STRYKER_TOW.get(), StrykerTowRenderer::new);
        event.registerEntityRenderer(ModEntities.STRYKER_MORTAR.get(), StrykerMortarRenderer::new);

        event.registerEntityRenderer(ModEntities.LITTLEBIRD.get(), LittlebirdRenderer::new);
        event.registerEntityRenderer(ModEntities.LITTLEBIRD_ARMED.get(), LittlebirdArmedRenderer::new);

        event.registerEntityRenderer(ModEntities.BMP1.get(), BMP1Renderer::new);
        event.registerEntityRenderer(ModEntities.BMP1U.get(), BMP1URenderer::new);
        event.registerEntityRenderer(ModEntities.BMP1AM.get(), BMP1AMRenderer::new);

        event.registerEntityRenderer(ModEntities.BMP2.get(), BMP2Renderer::new);

        event.registerEntityRenderer(ModEntities.AAVP.get(), AAVPRenderer::new);

        event.registerEntityRenderer(ModEntities.LAV25.get(), Lav25Renderer::new);

        event.registerEntityRenderer(ModEntities.T72AV.get(), T72AVRenderer::new);

        event.registerEntityRenderer(ModEntities.URAL.get(), UralRenderer::new);
        event.registerEntityRenderer(ModEntities.URAL_GRAD.get(), UralGradRenderer::new);

        event.registerEntityRenderer(ModEntities.KAMAZ.get(), KamazRenderer::new);

        event.registerEntityRenderer(ModEntities.VIPER.get(), ViperRenderer::new);

        event.registerEntityRenderer(ModEntities.GAZ_TIGR.get(), GazTigrRenderer::new);
        event.registerEntityRenderer(ModEntities.GAZ_TIGR_RWS.get(), GazTigrRWSRenderer::new);
        event.registerEntityRenderer(ModEntities.GAZ_TIGR_MG.get(), GazTigrMGRenderer::new);
        event.registerEntityRenderer(ModEntities.GAZ_TIGR_GL.get(), GazTigrGLRenderer::new);

        event.registerEntityRenderer(ModEntities.HUEY.get(), HueyRenderer::new);
        event.registerEntityRenderer(ModEntities.HUEY_ROCKETS.get(), HueyRocketsRenderer::new);
        event.registerEntityRenderer(ModEntities.HUEY_DOOR_GUNNER_M60.get(), HueyDoorGunnerM60Renderer::new);
        event.registerEntityRenderer(ModEntities.HUEY_DOOR_GUNNER_M134.get(), HueyDoorGunnerM134Renderer::new);
        event.registerEntityRenderer(ModEntities.VENOM.get(), VenomRenderer::new);

        event.registerEntityRenderer(ModEntities.NOVATOR.get(), NovatorRenderer::new);

        event.registerEntityRenderer(ModEntities.MATV.get(), MATVRenderer::new);
        event.registerEntityRenderer(ModEntities.MATV_TOW.get(), MATVTOWRenderer::new);
        event.registerEntityRenderer(ModEntities.MATV_CROW.get(), MATVCrowsRenderer::new);
        event.registerEntityRenderer(ModEntities.MATV_9IN1.get(), MATV9In1Renderer::new);

        event.registerEntityRenderer(ModEntities.HUMVEE.get(), HumveeRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMVEE_TOW.get(), HumveeTOWRenderer::new);

        event.registerEntityRenderer(ModEntities.BTR82.get(), BTR82Renderer::new);

        event.registerEntityRenderer(ModEntities.MI17.get(), MI17Renderer::new);

        event.registerEntityRenderer(ModEntities.FMTV.get(), FMTVRenderer::new);

        event.registerEntityRenderer(ModEntities.JOHN_DEERE.get(), JohnDeereRenderer::new);

        // Projectiles
        event.registerEntityRenderer(ModEntities.LOCK_ON_HELLFIRE.get(), LockOnHellfireRenderer::new);
        event.registerEntityRenderer(ModEntities.WIRE_GUIDED_HELLFIRE.get(), WireGuidedHellfireRenderer::new);

        event.registerEntityRenderer(ModEntities.SIDEWINDER.get(), SidewinderRenderer::new);

        event.registerEntityRenderer(ModEntities.MALYUTKA.get(), MalyutkaRenderer::new);

        // Meme Vehicles
        event.registerEntityRenderer(ModEntities.BIGBIRD.get(), BigBirdRenderer::new);
        event.registerEntityRenderer(ModEntities.LA_HUMVEE.get(), LaHumveeRenderer::new);
        event.registerEntityRenderer(ModEntities.T14_ARMATA.get(), WolfRenderer::new);
        // Trailers
        event.registerEntityRenderer(ModEntities.EXAMPLE_TRAILER.get(), ExampleTrailerRenderer::new);
        event.registerEntityRenderer(ModEntities.SEEDER.get(), SeederRenderer::new);
    }
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll(FcpDriverOverlay.ID, FcpDriverOverlay.INSTANCE);
        event.registerBelowAll(FcpPilotOverlay.ID, FcpPilotOverlay.INSTANCE);
    }

}

