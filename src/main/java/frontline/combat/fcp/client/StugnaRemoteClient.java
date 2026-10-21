package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.atsuishio.superbwarfare.init.ModKeyMappings;
import com.mojang.blaze3d.systems.RenderSystem;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Stugna.StugnaLauncherEntity;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.StugnaAimPacket;
import frontline.combat.fcp.network.StugnaRemoteActionPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Client side of Stugna-P remote control, done like SBW's drone monitor: the player keeps standing
 * at the control panel while the camera is moved to the launcher's sight every frame. The camera is
 * marked detached (as in third person / the drone view) so the operator's own body is still drawn
 * at the panel. The body stays facing the panel: mouse look instead moves a separate sight aim (at
 * quarter sensitivity, clamped to the turret's elevation limits) that the camera and the launcher's
 * turret follow (StugnaAimPacket / StugnaLauncherEntity#adjustTurretAngle).
 *
 * <p>Controls: left-click fires, the scroll wheel zooms (1x-6x), SBW's thermal-imaging key toggles
 * thermal (see ThermalImagingRemoteMixin), sneak disconnects. Walking, the vanilla crosshair and the
 * hotbar are suppressed; the drone camera frame, reticle and readouts are drawn instead.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class StugnaRemoteClient {

    public static final String OVERLAY_ID = "fcp_stugna_remote";
    public static final IGuiOverlay OVERLAY = StugnaRemoteClient::renderOverlay;

    // Scroll-wheel zoom, like the drone: 1x to 6x in whole steps.
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 6;

    // Same camera frame and reticle as SBW's drone HUD.
    private static final ResourceLocation TV_FRAME =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/tv_frame.png");
    private static final ResourceLocation RETICLE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/crosshair/third_camera.png");
    private static final ResourceLocation ZOOM_SCALE =
            new ResourceLocation("superbwarfare", "textures/overlay/drone/drone_fov.png");
    private static final ResourceLocation ZOOM_MARKER =
            new ResourceLocation("superbwarfare", "textures/overlay/drone/drone_fov_move.png");
    /** Beyond this the RANGE readout shows "---m", as on the drone. */
    private static final double MAX_RANGE = 500.0;

    /** Vanilla HUD pieces hidden while operating. */
    private static final Set<ResourceLocation> HIDDEN_OVERLAYS = Set.of(
            VanillaGuiOverlay.CROSSHAIR.id(),
            VanillaGuiOverlay.HOTBAR.id(),
            VanillaGuiOverlay.EXPERIENCE_BAR.id(),
            VanillaGuiOverlay.JUMP_BAR.id(),
            VanillaGuiOverlay.ITEM_NAME.id());

    /** Entity id of the launcher being operated, or -1. */
    private static int launcherId = -1;
    private static int zoom = MIN_ZOOM;
    /** Smoothed zoom used for the FOV and the zoom scale. */
    private static double zoomLerp = MIN_ZOOM;
    private static boolean sneakWasDown;

    /** Mouse-look sensitivity of the sight, relative to normal look. */
    private static final float AIM_SENSITIVITY = 0.25f;
    /** The operator's body rotation, held fixed (facing the panel) while linked. */
    private static float lockYaw, lockPitch;
    /** The sight aim (world yaw / pitch), moved by the mouse instead of the body. */
    private static float aimYaw, aimPitch;
    private static float sentYaw = Float.NaN, sentPitch = Float.NaN;
    private static boolean thermalKeyWasDown;
    /** Set while asking SBW whether real goggles are worn, so the mixin answers truthfully. */
    private static boolean checkingRealGoggles;

    // Camera#setPosition(Vec3) is protected and Camera#detached private; SRG names, remapped in dev.
    private static final Method SET_CAMERA_POSITION =
            ObfuscationReflectionHelper.findMethod(Camera.class, "m_90581_", Vec3.class);
    private static final Field CAMERA_DETACHED =
            ObfuscationReflectionHelper.findField(Camera.class, "f_90560_");

    private StugnaRemoteClient() {
    }

    /** Called from StugnaRemoteStatePacket. */
    public static void setLauncher(int id) {
        launcherId = id;
        zoom = MIN_ZOOM;
        zoomLerp = MIN_ZOOM;
        sneakWasDown = true; // ignore a sneak still held from before linking
        Player player = Minecraft.getInstance().player;
        if (id >= 0 && player != null) {
            lockYaw = player.getYRot();
            lockPitch = player.getXRot();
            aimYaw = lockYaw;
            aimPitch = lockPitch;
            sentYaw = sentPitch = Float.NaN;
        }
    }

    public static boolean isActive() {
        return launcherId >= 0;
    }

    /** Whether ThermalImagingRemoteMixin should report thermal goggles for the linked operator. */
    public static boolean grantsThermalSight() {
        return isActive() && !checkingRealGoggles;
    }

    private static boolean wearsRealGoggles() {
        checkingRealGoggles = true;
        try {
            return ClientEventHandler.hasThermalImagingGoggles();
        } finally {
            checkingRealGoggles = false;
        }
    }

    private static StugnaLauncherEntity launcher() {
        Minecraft mc = Minecraft.getInstance();
        if (launcherId < 0 || mc.level == null) {
            return null;
        }
        Entity entity = mc.level.getEntity(launcherId);
        return entity instanceof StugnaLauncherEntity stugna ? stugna : null;
    }

    /** Player pitch (negative = up) allowed by the turret's elevation limits. */
    private static float clampPitch(StugnaLauncherEntity stugna, float xRot) {
        return Mth.clamp(xRot, -stugna.getTurretMaxPitch(), -stugna.getTurretMinPitch());
    }

    private static void holdBody(Player player) {
        player.setYRot(lockYaw);
        player.setXRot(lockPitch);
        player.yRotO = lockYaw;
        player.xRotO = lockPitch;
        player.setYHeadRot(lockYaw);
        player.yHeadRotO = lockYaw;
    }

    /** Where the barrel actually points; the camera is locked to it, like SBW's "Direction": "Barrel". */
    private static Vec3 barrelVector(StugnaLauncherEntity stugna, float partialTick) {
        return stugna.getBarrelVector(partialTick).normalize();
    }

    // ---------------------------------------------------------------------------------------------
    // Camera
    // ---------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        StugnaLauncherEntity stugna = launcher();
        Player player = Minecraft.getInstance().player;
        if (stugna == null || player == null) {
            return;
        }
        // Mouse look has just been applied to the player: move the sight by that much (at reduced
        // sensitivity) and put the body back, so the operator keeps facing the panel.
        aimYaw += (player.getYRot() - lockYaw) * AIM_SENSITIVITY;
        aimPitch = clampPitch(stugna, aimPitch + (player.getXRot() - lockPitch) * AIM_SENSITIVITY);
        holdBody(player);
        // The view looks down the barrel, which turns towards the aim at the turret's own speed.
        Vec3 barrel = barrelVector(stugna, (float) event.getPartialTick());
        double horizontal = Math.sqrt(barrel.x * barrel.x + barrel.z * barrel.z);
        event.setYaw((float) Math.toDegrees(Math.atan2(-barrel.x, barrel.z)));
        event.setPitch((float) Math.toDegrees(-Math.atan2(barrel.y, horizontal)));
        Vec3 sight = VehicleVecUtils.getCameraPos(stugna, player, (float) event.getPartialTick());
        try {
            SET_CAMERA_POSITION.invoke(event.getCamera(), sight);
            // Detached, like third person / the drone view, so our own body renders at the panel.
            CAMERA_DETACHED.setBoolean(event.getCamera(), true);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        if (isActive()) {
            event.setNewFovModifier((float) (event.getNewFovModifier() / zoomLerp));
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (isActive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        if (isActive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        NamedGuiOverlay overlay = event.getOverlay();
        if (isActive() && HIDDEN_OVERLAYS.contains(overlay.id())) {
            event.setCanceled(true);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Input
    // ---------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!isActive()) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);
        if (event.isAttack()) {
            FCPNetwork.FCP_HANDLER.sendToServer(new StugnaRemoteActionPacket(StugnaRemoteActionPacket.FIRE));
        }
    }

    // The scroll wheel zooms the sight instead of changing the hotbar slot.
    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (!isActive() || Minecraft.getInstance().screen != null) {
            return;
        }
        event.setCanceled(true);
        zoom = Mth.clamp(zoom + (event.getScrollDelta() > 0 ? 1 : -1), MIN_ZOOM, MAX_ZOOM);
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!isActive()) {
            return;
        }
        var input = event.getInput();
        boolean sneak = input.shiftKeyDown;
        if (sneak && !sneakWasDown) {
            FCPNetwork.FCP_HANDLER.sendToServer(new StugnaRemoteActionPacket(StugnaRemoteActionPacket.DISCONNECT));
        }
        sneakWasDown = sneak;
        // The operator stays put at the panel.
        input.forwardImpulse = 0;
        input.leftImpulse = 0;
        input.up = input.down = input.left = input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        StugnaLauncherEntity stugna = launcher();
        if (mc.level != null && stugna == null) {
            // Launcher unloaded or gone; the server ends the session too.
            setLauncher(-1);
            return;
        }
        if (mc.player != null) {
            holdBody(mc.player);
        }
        // Thermal sight. SBW's own key handler only reacts to a thermal vehicle seat or goggles found in
        // the curio slots, so toggle its flag here (unless real goggles are worn - then SBW does it).
        // ThermalImagingRemoteMixin then keeps SBW from switching it straight back off.
        boolean thermalKey = ModKeyMappings.ACTIVE_THERMAL_IMAGING.isDown();
        if (thermalKey && !thermalKeyWasDown && mc.screen == null && mc.player != null && !wearsRealGoggles()) {
            ClientEventHandler.activeThermalImaging = !ClientEventHandler.activeThermalImaging;
        }
        thermalKeyWasDown = thermalKey;
        if (aimYaw != sentYaw || aimPitch != sentPitch) {
            FCPNetwork.FCP_HANDLER.sendToServer(new StugnaAimPacket(aimYaw, aimPitch));
            sentYaw = aimYaw;
            sentPitch = aimPitch;
        }
        zoomLerp += (zoom - zoomLerp) * 0.4;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        setLauncher(-1);
    }

    // ---------------------------------------------------------------------------------------------
    // HUD
    // ---------------------------------------------------------------------------------------------

    private static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        StugnaLauncherEntity stugna = launcher();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (stugna == null || player == null || mc.options.hideGui) {
            return;
        }
        int cx = width / 2;
        int cy = height / 2;
        var poseStack = graphics.pose();
        poseStack.pushPose();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // CRT camera frame over the whole screen.
        graphics.blit(TV_FRAME, 0, 0, 0f, 0f, width, height, width, height);

        // The rest mirrors SBW's drone HUD layout: reticle, zoom scale, then the readouts.
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
            var data = stugna.getGunData(0);
            if (data != null) {
                ammo = data.ammo.get() + " / " + data.get(com.atsuishio.superbwarfare.data.gun.GunProp.MAGAZINE);
            }
        } catch (RuntimeException ignored) {
        }
        graphics.drawString(mc.font, Component.translatable("tips.superbwarfare.drone.ammo").getString() + ammo,
                cx + 12, cy - 37, -1, false);

        Vec3 sight = VehicleVecUtils.getCameraPos(stugna, player, partialTick);
        Vec3 end = sight.add(barrelVector(stugna, partialTick).scale(MAX_RANGE));
        HitResult hit = player.level().clip(new ClipContext(sight, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        String range = hit.getType() == HitResult.Type.MISS ? "---m" : String.format("%.1fm", hit.getLocation().distanceTo(sight));
        graphics.drawString(mc.font, Component.translatable("tips.superbwarfare.drone.range").getString() + range,
                cx + 12, cy - 28, -1, false);

        // HEALTH (the launcher's) and DISTANCE (operator to launcher) below the reticle.
        graphics.drawString(mc.font, Component.translatable("tips.superbwarfare.drone.health").getString()
                        + String.format("%.0f / %.0f", stugna.getHealth(), stugna.getMaxHealth()),
                cx - 77, cy + 33, -1, false);
        graphics.drawString(mc.font, Component.translatable("tips.superbwarfare.drone.distance").getString()
                        + String.format("%.0fm", player.distanceTo(stugna)),
                cx + 10, cy + 33, -1, false);

        if (ClientEventHandler.activeThermalImaging) {
            graphics.drawString(mc.font, "THERMAL", cx - 77, cy - 37, 0xFF66FF66, false);
        }

        graphics.drawCenteredString(mc.font, "SHIFT to Disconnect", cx, height - 28, 0xAAAAAA);
    }
}
