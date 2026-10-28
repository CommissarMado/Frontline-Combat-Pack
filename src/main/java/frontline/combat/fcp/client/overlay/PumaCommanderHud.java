package frontline.combat.fcp.client.overlay;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import frontline.combat.fcp.FCP;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/**
 * The commander's sight on the Puma (both variants) and the BMP-2M: the same camera HUD as the
 * Stugna-P remote (StugnaRemoteClient) -
 * TV frame, reticle, scroll-wheel zoom (1x-6x) with its scale, the RANGE readout and a THERMAL tag -
 * without its ammo, health and distance readouts. The commander seat is thermal-capable in the
 * vehicle JSON ("HasThermalImaging"), so SBW's own thermal key works there. SBW's vehicle HUD and the
 * vanilla crosshair/hotbar are hidden while in the seat, so only this HUD shows.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class PumaCommanderHud {

    public static final String OVERLAY_ID = "fcp_puma_commander";
    public static final IGuiOverlay OVERLAY = PumaCommanderHud::renderOverlay;

    /** Vehicles whose commander (passenger weapon station) seat gets this sight. */
    private static final Set<String> VEHICLES = Set.of("fcp:puma", "fcp:puma_atgm", "fcp:m2a3", "fcp:m2a3_busk", "fcp:bmp2m", "fcp:t72m2_moderna");

    private static final int MIN_ZOOM = 1, MAX_ZOOM = 6;
    private static final double MAX_RANGE = 500.0;

    // Same textures as the Stugna remote / SBW's drone HUD.
    private static final ResourceLocation TV_FRAME =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/tv_frame.png");
    private static final ResourceLocation RETICLE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/crosshair/third_camera.png");
    private static final ResourceLocation ZOOM_SCALE =
            new ResourceLocation("superbwarfare", "textures/overlay/drone/drone_fov.png");
    private static final ResourceLocation ZOOM_MARKER =
            new ResourceLocation("superbwarfare", "textures/overlay/drone/drone_fov_move.png");

    private static final Set<ResourceLocation> HIDDEN_VANILLA = Set.of(
            VanillaGuiOverlay.CROSSHAIR.id(),
            VanillaGuiOverlay.HOTBAR.id(),
            VanillaGuiOverlay.EXPERIENCE_BAR.id(),
            VanillaGuiOverlay.JUMP_BAR.id(),
            VanillaGuiOverlay.ITEM_NAME.id());

    private static int zoom = MIN_ZOOM;
    private static double zoomLerp = MIN_ZOOM;

    private PumaCommanderHud() {
    }

    /** The vehicle whose commander sight the local player is looking through (first person), or null. */
    private static VehicleEntity commandedVehicle() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !mc.options.getCameraType().isFirstPerson()) return null;
        if (!(player.getVehicle() instanceof VehicleEntity vehicle)) return null;
        if (!VEHICLES.contains(EntityType.getKey(vehicle.getType()).toString())) return null;
        return vehicle.getSeatIndex(player) == vehicle.computed().getPassengerWeaponStationControllerIndex() ? vehicle : null;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (commandedVehicle() == null) {
            zoom = MIN_ZOOM;
            zoomLerp = MIN_ZOOM;
            return;
        }
        zoomLerp += (zoom - zoomLerp) * 0.4;
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (commandedVehicle() == null || Minecraft.getInstance().screen != null) return;
        event.setCanceled(true);
        zoom = Mth.clamp(zoom + (event.getScrollDelta() > 0 ? 1 : -1), MIN_ZOOM, MAX_ZOOM);
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        if (commandedVehicle() != null) {
            event.setNewFovModifier((float) (event.getNewFovModifier() / zoomLerp));
        }
    }

    @SubscribeEvent
    public static void onGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (commandedVehicle() == null) return;
        ResourceLocation id = event.getOverlay().id();
        if ("superbwarfare".equals(id.getNamespace()) || HIDDEN_VANILLA.contains(id)) {
            event.setCanceled(true);
        }
    }

    private static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        VehicleEntity vehicle = commandedVehicle();
        if (vehicle == null || mc.options.hideGui) return;
        int cx = width / 2, cy = height / 2;
        var poseStack = graphics.pose();
        poseStack.pushPose();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        graphics.blit(TV_FRAME, 0, 0, 0f, 0f, width, height, width, height);
        graphics.blit(RETICLE, cx - 16, cy - 16, 0f, 0f, 32, 32, 32, 32);
        graphics.blit(ZOOM_SCALE, cx + 100, cy - 64, 0f, 0f, 64, 129, 64, 129);
        int zoomOffset = (int) ((zoomLerp - 1) * 23.8);
        graphics.blit(ZOOM_MARKER, cx + 100, cy - 64 - zoomOffset, 0f, 0f, 64, 129, 64, 129);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        poseStack.popPose();

        graphics.drawString(mc.font, String.format("%.1fx", zoomLerp), cx + 144, cy + 56 - zoomOffset, -1, false);

        // RANGE along the sight line, from the commander's camera.
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 eye = camera.getPosition();
        Vec3 end = eye.add(new Vec3(camera.getLookVector()).scale(MAX_RANGE));
        HitResult hit = vehicle.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        String range = hit.getType() == HitResult.Type.MISS ? "---m" : String.format("%.1fm", hit.getLocation().distanceTo(eye));
        graphics.drawString(mc.font, Component.translatable("tips.superbwarfare.drone.range").getString() + range,
                cx + 12, cy - 28, -1, false);

        if (ClientEventHandler.activeThermalImaging) {
            graphics.drawString(mc.font, "THERMAL", cx - 77, cy - 37, 0xFF66FF66, false);
        }
    }
}
