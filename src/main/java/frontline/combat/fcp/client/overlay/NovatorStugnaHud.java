package frontline.combat.fcp.client.overlay;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import frontline.combat.fcp.FCP;
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
 * The Novator Stugna's gunner sight: the emplaced Stugna-P's remote HUD (StugnaRemoteClient) - TV frame, reticle,
 * scroll-wheel zoom (1x-6x) with its scale, AMMO / RANGE readouts and the THERMAL tag. The remote's HEALTH, operator
 * DISTANCE and "SHIFT to Disconnect" don't apply in the vehicle. The gunner seat is thermal-capable in the vehicle JSON,
 * so SBW's own thermal key works. In the sight SBW's seat list, bars and weapon box are drawn over it (user); SBW's
 * crosshair and land-HUD frame (LandVehicleHudStatsMixin) and the vanilla crosshair/hotbar are hidden.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class NovatorStugnaHud {

    public static final String OVERLAY_ID = "fcp_novator_stugna";
    public static final IGuiOverlay OVERLAY = NovatorStugnaHud::renderOverlay;

    private static final Set<String> VEHICLES = Set.of("fcp:novator_stugna");

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

    private static final ResourceLocation SBW_CROSSHAIR = new ResourceLocation("superbwarfare", "vehicle_crosshair");

    private static int zoom = MIN_ZOOM;
    private static double zoomLerp = MIN_ZOOM;

    private NovatorStugnaHud() {
    }

    /** The vehicle whose launcher sight the local player is looking through (first person), or null. */
    public static VehicleEntity gunnedVehicle() {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson() ? gunnerSeat() : null;
    }

    /** The vehicle whose gunner seat the local player is in (any camera), or null. */
    private static VehicleEntity gunnerSeat() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return null;
        if (!(player.getVehicle() instanceof VehicleEntity vehicle)) return null;
        if (!VEHICLES.contains(EntityType.getKey(vehicle.getType()).toString())) return null;
        return vehicle.getSeatIndex(player) == vehicle.computed().getTurretControllerIndex() ? vehicle : null;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // The gunner only ever looks through the launcher's sight, like the emplaced Stugna's operator: no third
        // person (with it, SBW's ADS skipped this HUD - user).
        Minecraft mc = Minecraft.getInstance();
        if (gunnerSeat() != null && !mc.options.getCameraType().isFirstPerson()) {
            mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
        }
        if (event.phase != TickEvent.Phase.END) return;
        if (gunnedVehicle() == null) {
            zoom = MIN_ZOOM;
            zoomLerp = MIN_ZOOM;
            return;
        }
        zoomLerp += (zoom - zoomLerp) * 0.4;
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (gunnedVehicle() == null || Minecraft.getInstance().screen != null) return;
        event.setCanceled(true);
        zoom = Mth.clamp(zoom + (event.getScrollDelta() > 0 ? 1 : -1), MIN_ZOOM, MAX_ZOOM);
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        if (gunnedVehicle() != null) {
            event.setNewFovModifier((float) (event.getNewFovModifier() / zoomLerp));
        }
    }

    @SubscribeEvent
    public static void onGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        // In the sight SBW's seat list, bars and weapon box stay (user); only its crosshair goes (and its land-HUD
        // frame, in LandVehicleHudStatsMixin). Third person is SBW's as usual.
        if (gunnedVehicle() == null) return;
        ResourceLocation id = event.getOverlay().id();
        if (SBW_CROSSHAIR.equals(id) || HIDDEN_VANILLA.contains(id)) {
            event.setCanceled(true);
        }
    }

    private static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        VehicleEntity vehicle = gunnedVehicle();
        if (vehicle == null || player == null || mc.options.hideGui) return;
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

        // AMMO / RANGE above-right of the reticle.
        String ammo = "-";
        try {
            var data = vehicle.getGunData(vehicle.getSeatIndex(player));
            if (data != null) {
                ammo = data.ammo.get() + " / " + data.get(com.atsuishio.superbwarfare.data.gun.GunProp.MAGAZINE);
            }
        } catch (RuntimeException ignored) {
        }
        graphics.drawString(mc.font, Component.translatable("tips.superbwarfare.drone.ammo").getString() + ammo,
                cx + 12, cy - 37, -1, false);

        Vec3 sight = VehicleVecUtils.getCameraPos(vehicle, player, partialTick);
        Vec3 end = sight.add(vehicle.getBarrelVector(partialTick).scale(MAX_RANGE));
        HitResult hit = player.level().clip(new ClipContext(sight, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        String range = hit.getType() == HitResult.Type.MISS ? "---m" : String.format("%.1fm", hit.getLocation().distanceTo(sight));
        graphics.drawString(mc.font, Component.translatable("tips.superbwarfare.drone.range").getString() + range,
                cx + 12, cy - 28, -1, false);

        if (ClientEventHandler.activeThermalImaging) {
            graphics.drawString(mc.font, "THERMAL", cx - 77, cy - 37, 0xFF66FF66, false);
        }
    }
}
