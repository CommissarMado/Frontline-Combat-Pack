package frontline.combat.fcp.client.overlay;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.client.overlay.VehicleHudOverlay;
import com.atsuishio.superbwarfare.client.overlay.VehicleMainWeaponHudOverlay;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.atsuishio.superbwarfare.init.ModKeyMappings;
import com.atsuishio.superbwarfare.tools.FormatTool;
import com.atsuishio.superbwarfare.tools.MathTool;
import com.atsuishio.superbwarfare.tools.VectorToolKt;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class FcpPilotOverlay implements IGuiOverlay {

    public static final FcpPilotOverlay INSTANCE = new FcpPilotOverlay();
    public static final String ID = "fcp_pilot_hud";

    private static final ResourceLocation HELI_BASE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/heli_base.png");
    private static final ResourceLocation HELI_DRIVER_ANGLE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/heli_driver_angle.png");
    private static final ResourceLocation ROLL_IND =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/roll_ind.png");
    private static final ResourceLocation HELI_POWER_RULER =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/heli_power_ruler.png");
    private static final ResourceLocation HELI_POWER =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/heli_power.png");
    private static final ResourceLocation HELI_VY_MOVE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/heli_vy_move.png");
    private static final ResourceLocation SPEED_FRAME =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/speed_frame.png");
    private static final ResourceLocation CROSSHAIR_IND =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/crosshair_ind.png");
    private static final ResourceLocation COMPASS =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/base/compass.png");
    private static final ResourceLocation HUD_LINE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/aircraft/hud_line.png");

    // Per-vehicle seat list: [ vehicle, [seats that get the pilot HUD] ].
    // Keys are the entity id path (no namespace), e.g. "littlebird" matches fcp:littlebird.
    private static final Map<String, List<Integer>> PILOT_OVERLAY_VEHICLES = Map.ofEntries(
            Map.entry("littlebird",       List.of(0, 1)),
            Map.entry("littlebird_armed", List.of(1)),
            Map.entry("venom", List.of(0, 1)),
            Map.entry("huey", List.of(0, 1)),
            Map.entry("huey_rockets", List.of(1)),
            Map.entry("huey_door_gunner_m60", List.of(0, 2)),
            Map.entry("huey_door_gunner_m134", List.of(2))
    );

    // Smoothed HUD state (matches the default helicopter HUD's lerped values).
    private float scopeScale = 1f;
    private float lerpVy = 1f;
    private float lerpPower = 0f;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick,
                       int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator() || mc.options.hideGui) return;
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return;

        var vehicle = player.getVehicle();
        if (!(vehicle instanceof VehicleEntity ve)) return;
        if (!ve.computed().getHudType().equals("@Helicopter")) return;

        // Look up which seats of this vehicle should show the pilot HUD.
        List<Integer> hudSeats = PILOT_OVERLAY_VEHICLES.get(EntityType.getKey(ve.getType()).getPath());
        if (hudSeats == null) return;

        int seatIndex = ve.getSeatIndex(player);
        if (!hudSeats.contains(seatIndex)) return;

        int color = ve.getHudColor();
        var poseStack = guiGraphics.pose();

        // Weapon data for this seat (may be null on an unarmed seat -> ammo line is simply skipped).
        GunData data = ve.getGunData(seatIndex);

        poseStack.pushPose();

        // Proper alpha blending so the semi-transparent vignette / ladder pixels are respected.
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // --- Centered square that the base vignette / driver-angle reticle fill ---
        scopeScale = Mth.lerp(partialTick, scopeScale, 1f);
        float f = Math.min(screenWidth, screenHeight);
        float f1 = Math.min(screenWidth / f, screenHeight / f) * scopeScale;
        float i = Mth.floor(f * f1);
        float j = Mth.floor(f * f1);
        float k = (screenWidth - i) / 2f;
        float l = (screenHeight - j) / 2f;

        // --- Aim projection (crosshair indicator follows where the nose is pointing) ---
        Vec3 shootPos = ve.getShootPosForHud(player, partialTick);
        Vec3 shootDir = ve.getShootDirectionForHud(player, partialTick);
        double dis = computeAimDistance(ve, player, shootPos, shootDir, partialTick);
        Vec3 pos = shootPos.add(shootDir.scale(dis));
        Vec3 screenPos = VectorToolKt.worldToScreen(pos);
        float x = (float) screenPos.x;
        float y = (float) screenPos.y;

        double speed = ve.getDeltaMovement().length() * 72;
        lerpVy = Mth.lerp(0.021f * partialTick, lerpVy, (float) (ve.getDeltaMovement().y() * 20));

        // --- Base vignette ---
        RenderHelper.blit(poseStack, HELI_BASE, k, l, 0f, 0f, i, j, i, j, color);

        // --- Driver / nose angle reticle ---
        float diffY = -Mth.lerp(partialTick, ve.getTurretYRotO(), ve.getTurretYRot()) * 0.3f;
        float diffX = (float) (Mth.wrapDegrees(
                -VehicleVecUtils.getXRotFromVector(ve.getBarrelVector(partialTick))
                        - Mth.lerp(partialTick, ve.xRotO, ve.getXRot())) * 0.072f);
        RenderHelper.blit(poseStack, HELI_DRIVER_ANGLE, k + diffY, l + diffX, 0f, 0f, i, j, i, j, color);

        // --- Compass ---
        RenderHelper.blit(poseStack, COMPASS,
                screenWidth / 2f - 128, 6f,
                128 + (64f / 45 * ve.getYRot()), 0f,
                256f, 16f, 512f, 16f, color);

        // --- Pitch ladder (rotates against roll) ---
        poseStack.pushPose();
        poseStack.rotateAround(Axis.ZP.rotationDegrees(-ve.getRoll(partialTick)),
                screenWidth / 2f, screenHeight / 2f, 0f);
        float pitch = ve.getPitch(partialTick);
        RenderHelper.blit(poseStack, HUD_LINE,
                screenWidth / 2f - 144, screenHeight / 2f - 128,
                0f, 722.5f + 4.725f * pitch,
                288f, 256f, 288f, 1701f, color);
        poseStack.popPose();

        // --- Roll indicator ---
        poseStack.pushPose();
        poseStack.rotateAround(Axis.ZP.rotationDegrees(ve.getRoll(partialTick)),
                screenWidth / 2f, screenHeight / 2f - 56, 0f);
        RenderHelper.blit(poseStack, ROLL_IND,
                screenWidth / 2f - 8, screenHeight / 2f - 88,
                0f, 0f, 16f, 16f, 16f, 16f, color);
        poseStack.popPose();

        // --- Engine power ruler + fill ---
        RenderHelper.blit(poseStack, HELI_POWER_RULER,
                screenWidth / 2f + 100, screenHeight / 2f - 64,
                0f, 0f, 64f, 128f, 64f, 128f, color);

        float power = ve.getPower();
        lerpPower = Mth.lerp(0.5f * partialTick, lerpPower, power);
        RenderHelper.blit(poseStack, HELI_POWER,
                screenWidth / 2f + 130f, screenHeight / 2f - 64 + 124 - lerpPower * 980,
                0f, 0f, 4f, lerpPower * 980, 4f, lerpPower * 980, color);

        // --- Vertical speed indicator ---
        RenderHelper.blit(poseStack, HELI_VY_MOVE,
                screenWidth / 2f + 138, screenHeight / 2f - 3 - Mth.clamp(lerpVy * 3, -24f, 24f) * 2.5f,
                0f, 0f, 8f, 8f, 8f, 8f, color);
        guiGraphics.drawString(mc.font,
                Component.literal(FormatTool.format0D(lerpVy, "m/s")),
                screenWidth / 2 + 146,
                (int) (screenHeight / 2f - 3 - Mth.clamp(lerpVy * 3, -24f, 24f) * 2.5),
                (lerpVy < -12 ? -65536 : color), false);

        // --- Altitude ---
        guiGraphics.drawString(mc.font,
                Component.literal(FormatTool.format0D(ve.getY())),
                screenWidth / 2 + 104, screenHeight / 2, color, false);

        // --- Speed frame + readout ---
        RenderHelper.blit(poseStack, SPEED_FRAME,
                screenWidth / 2f - 144, screenHeight / 2f - 6,
                0f, 0f, 50f, 18f, 50f, 18f, color);
        guiGraphics.drawString(mc.font,
                Component.literal(FormatTool.format0D(speed, "km/h")),
                screenWidth / 2 - 140, screenHeight / 2, color, false);

        // --- Flare / decoy status ---
        if (ve.hasDecoy()) {
            if (ve.getDecoyReady()) {
                guiGraphics.drawString(mc.font,
                        Component.translatable("tips.superbwarfare.flare.ready").append(
                                Component.literal(" [" + ModKeyMappings.RELEASE_DECOY.getKey().getDisplayName().getString() + "]")),
                        screenWidth / 2 - 160, screenHeight / 2 - 50, color, false);
            } else {
                guiGraphics.drawString(mc.font,
                        Component.translatable("tips.superbwarfare.flare.reloading"),
                        screenWidth / 2 - 160, screenHeight / 2 - 50, 0xFF0000, false);
            }
        }

        // --- Ammo + heat (only when this seat actually controls a weapon) ---
        if (data != null) {
            Component ammo = ve.firstPersonAmmoComponent(data, player);
            int heat = ve.getWeaponHeat(player);
            guiGraphics.drawString(mc.font, ammo,
                    screenWidth / 2 - 160, screenHeight / 2 - 59,
                    MathTool.getGradientColor(color, 0xFF0000, heat, 2), false);
        }

        // --- Low / no power warning for electric airframes ---
        VehicleMainWeaponHudOverlay.INSTANCE.renderEnergyInfo(ve, guiGraphics, screenWidth, screenHeight, mc.font);

        // --- Crosshair indicator + kill markers at the projected aim point ---
        RenderHelper.blit(poseStack, CROSSHAIR_IND, x - 8, y - 8, 0f, 0f, 16f, 16f, 16f, 16f, color);
        VehicleHudOverlay.renderKillIndicatorDynamic(guiGraphics,
                x - 7.5f + (float) (2 * (Math.random() - 0.5f)),
                y - 7.5f + (float) (2 * (Math.random() - 0.5f)));

        poseStack.popPose();

        // Restore default render state so later overlays aren't affected.
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    /**
     * Replicates the default helicopter HUD's ranging logic locally: cast from the gun's
     * shoot position along the aim vector, preferring a looked-at entity over the block hit.
     */
    private double computeAimDistance(VehicleEntity ve, Player player, Vec3 shootPos, Vec3 shootDir, float partialTick) {
        double dis = 512.0;
        HitResult result = player.level().clip(new ClipContext(
                shootPos, shootPos.add(shootDir.scale(512.0)),
                ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
        dis = shootPos.distanceTo(result.getLocation());

        Entity looking = ve.getPlayerLookAtEntityOnVehicle(player, 512.0, partialTick);
        if (looking != null) {
            dis = shootPos.distanceTo(looking.position());
        }
        return dis;
    }
}