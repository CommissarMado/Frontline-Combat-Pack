package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity;
import frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.HimarsTvAimPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Client side of the HIMARS TV-guided GMLRS, flown like a drone. Once the local gunner's TV missile is in the air
 * (its synced pilot is this player), the camera rides the missile looking where it flies, and mouse look moves the
 * heading it steers for (EntityTurnMixin, then HimarsTvAimPacket). That heading can lead the missile by at most
 * {@link #MAX_LEAD} degrees, so it never winds up past what the missile can follow. The view goes back to the
 * launcher when the missile is gone or the gunner leaves the seat. Same camera trick and TV frame as the Stugna
 * remote (StugnaRemoteClient), and the picture is always thermal.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class HimarsTvClient {

    public static final String OVERLAY_ID = "fcp_himars_tv";
    public static final IGuiOverlay OVERLAY = HimarsTvClient::renderOverlay;

    private static final ResourceLocation TV_FRAME =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/tv_frame.png");
    private static final ResourceLocation RETICLE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/crosshair/third_camera.png");

    /** How far (degrees) the steered heading may lead the missile's actual heading. */
    private static final float MAX_LEAD = 20f;
    /** Steering per mouse-look unit: half of normal look (Entity#turn uses 0.15). */
    private static final float SENSITIVITY = 0.15f * 0.5f;
    /** Range readout limit (blocks). */
    private static final double MAX_RANGE = 1000;

    /** The missile being flown, or -1. */
    private static int missileId = -1;
    private static float aimYaw, aimPitch;
    private static float sentYaw = Float.NaN, sentPitch = Float.NaN;

    // Camera#setPosition(Vec3) is protected and Camera#detached private; SRG names, remapped in dev.
    private static final Method SET_CAMERA_POSITION =
            ObfuscationReflectionHelper.findMethod(Camera.class, "m_90581_", Vec3.class);
    private static final Field CAMERA_DETACHED =
            ObfuscationReflectionHelper.findField(Camera.class, "f_90560_");

    private HimarsTvClient() {
    }

    public static boolean isActive() {
        return missile() != null;
    }

    /** Whether {@code entity} is the missile the local player is flying (its renderer hides it from the inside). */
    public static boolean isPiloted(Entity entity) {
        return missileId >= 0 && entity.getId() == missileId;
    }

    private static HimarsMissileEntity missile() {
        Minecraft mc = Minecraft.getInstance();
        if (missileId < 0 || mc.level == null) return null;
        return mc.level.getEntity(missileId) instanceof HimarsMissileEntity m && m.isAlive() ? m : null;
    }

    /** From EntityTurnMixin: true when the mouse look was taken for steering. */
    public static boolean consumeTurn(Entity entity, double yRot, double xRot) {
        Minecraft mc = Minecraft.getInstance();
        if (entity != mc.player) return false;
        HimarsMissileEntity missile = missile();
        if (missile == null) return false;
        aimYaw += (float) yRot * SENSITIVITY;
        aimPitch += (float) xRot * SENSITIVITY;
        clampLead(missile);
        return true;
    }

    private static void clampLead(HimarsMissileEntity missile) {
        float yaw = missile.getYRot(), pitch = missile.getXRot();
        aimYaw = yaw + Mth.clamp(Mth.wrapDegrees(aimYaw - yaw), -MAX_LEAD, MAX_LEAD);
        aimPitch = Mth.clamp(pitch + Mth.clamp(aimPitch - pitch, -MAX_LEAD, MAX_LEAD), -90f, 90f);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            missileId = -1;
            return;
        }
        HimarsMissileEntity missile = missile();
        if (missile == null || missile.pilotId() != player.getId()) {
            missileId = -1;
            missile = null;
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e instanceof HimarsMissileEntity m && m.isAlive() && m.pilotId() == player.getId()) {
                    missile = m;
                    break;
                }
            }
            if (missile == null) return;
            missileId = missile.getId();
            aimYaw = missile.getYRot();
            aimPitch = missile.getXRot();
            sentYaw = sentPitch = Float.NaN;
        }
        clampLead(missile);
        if (aimYaw != sentYaw || aimPitch != sentPitch) {
            FCPNetwork.FCP_HANDLER.sendToServer(new HimarsTvAimPacket(missileId, aimYaw, aimPitch));
            sentYaw = aimYaw;
            sentPitch = aimPitch;
        }
    }

    /** Tells HimarsEntity to hold still on this client while the view is away (see HimarsEntity#tick). */
    @SubscribeEvent
    public static void onClientTickStart(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) HimarsEntity.localTvViewAway = isActive();
        if (event.phase == TickEvent.Phase.START) debugLog();
        if (event.phase == TickEvent.Phase.START) forceThermal();
    }

    /** The thermal setting from before the flight, restored after it; null while not flying. */
    private static Boolean thermalBefore;

    /**
     * The missile camera is a thermal seeker: thermal is forced on for the whole flight (ThermalImagingRemoteMixin
     * lets SBW render it here), and the player's own setting comes back afterwards.
     */
    private static void forceThermal() {
        if (isActive()) {
            if (thermalBefore == null) thermalBefore = ClientEventHandler.activeThermalImaging;
            ClientEventHandler.activeThermalImaging = true;
        } else if (thermalBefore != null) {
            ClientEventHandler.activeThermalImaging = thermalBefore;
            thermalBefore = null;
        }
    }

    // TEMPORARY diagnostics for the TV-return sinking bug: remove once found.
    private static final org.slf4j.Logger DBG = com.mojang.logging.LogUtils.getLogger();
    private static int dbgTick, dbgUntil;

    private static void debugLog() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;
        dbgTick++;
        if (isActive()) dbgUntil = dbgTick + 200;
        if (dbgTick > dbgUntil || dbgTick % 5 != 0) return;
        Entity vehicle = player.getVehicle();
        DBG.info("[HIMARS-DBG] LOCAL t={} active={} player y={} chunkLoaded={} vehicle={} vehicleY={} vehicleRemoved={}",
                dbgTick, isActive(), String.format("%.2f", player.getY()), mc.level.hasChunkAt(player.blockPosition()),
                vehicle == null ? "none" : vehicle.getId(), vehicle == null ? "-" : String.format("%.2f", vehicle.getY()),
                vehicle != null && vehicle.isRemoved());
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        HimarsMissileEntity missile = missile();
        if (missile == null) return;
        float pt = (float) event.getPartialTick();
        float yaw = Mth.rotLerp(pt, missile.yRotO, missile.getYRot());
        float pitch = Mth.lerp(pt, missile.xRotO, missile.getXRot());
        event.setYaw(yaw);
        event.setPitch(pitch);
        event.setRoll(0);
        Vec3 eye = missile.getPosition(pt).add(Vec3.directionFromRotation(pitch, yaw).scale(0.6));
        try {
            SET_CAMERA_POSITION.invoke(event.getCamera(), eye);
            CAMERA_DETACHED.setBoolean(event.getCamera(), true);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (isActive()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        if (isActive()) event.setCanceled(true);
    }

    /** Only the TV picture (and chat) while flying: the gunner's vehicle HUD would sit over it. */
    @SubscribeEvent
    public static void onGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (!isActive()) return;
        ResourceLocation id = event.getOverlay().id();
        if (!id.getPath().equals(OVERLAY_ID) && !id.equals(VanillaGuiOverlay.CHAT_PANEL.id())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!isActive()) return;
        event.setCanceled(true);
        event.setSwingHand(false);
    }

    /** The scroll wheel would switch the launcher's mode mid-flight. */
    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (isActive() && Minecraft.getInstance().screen == null) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        missileId = -1;
    }

    private static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        HimarsMissileEntity missile = missile();
        Minecraft mc = Minecraft.getInstance();
        if (missile == null || mc.player == null || mc.options.hideGui) return;
        int cx = width / 2, cy = height / 2;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(TV_FRAME, 0, 0, 0f, 0f, width, height, width, height);
        graphics.blit(RETICLE, cx - 16, cy - 16, 0f, 0f, 32, 32, 32, 32);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        Vec3 pos = missile.getPosition(partialTick);
        Vec3 look = missile.getViewVector(partialTick);
        HitResult hit = missile.level().clip(new ClipContext(pos, pos.add(look.scale(MAX_RANGE)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, missile));
        String range = hit.getType() == HitResult.Type.MISS ? "---m" : String.format("%.0fm", hit.getLocation().distanceTo(pos));
        graphics.drawString(mc.font, "GMLRS TV", cx - 77, cy - 37, 0xFF66FF66, false);
        graphics.drawString(mc.font, "RANGE " + range, cx + 12, cy - 37, -1, false);
        graphics.drawString(mc.font, String.format("ALT %.0fm", pos.y - mc.player.getY()), cx + 12, cy - 28, -1, false);
        graphics.drawString(mc.font, String.format("DIST %.0fm", pos.distanceTo(mc.player.position())), cx + 10, cy + 33, -1, false);
    }
}
