package frontline.combat.fcp.client.overlay;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.client.overlay.DecoyOverlayHelper;
import frontline.combat.fcp.entity.vehicle.GunnerSmoke;
import com.atsuishio.superbwarfare.tools.FormatTool;
import com.atsuishio.superbwarfare.tools.MathTool;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.Gui;
import frontline.combat.fcp.client.overlay.IGuiOverlay;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class FcpDriverOverlay implements IGuiOverlay {

    public static final FcpDriverOverlay INSTANCE = new FcpDriverOverlay();
    public static final String ID = "fcp_driver_hud";

    private static final ResourceLocation DRIVER_FRAME =
            ResourceLocation.fromNamespaceAndPath("fcp", "textures/overlay/vehicle/frame/driver.png");
    private static final ResourceLocation COMPASS =
            ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/overlay/vehicle/base/compass.png");
    private static final ResourceLocation ROLL_IND =
            ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/overlay/vehicle/helicopter/roll_ind.png");
    private static final ResourceLocation LINE =
            ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/overlay/vehicle/land/line.png");
    private static final ResourceLocation BODY =
            ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/overlay/vehicle/land/body.png");
    private static final ResourceLocation LEFT_WHEEL =
            ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/overlay/vehicle/land/left_wheel.png");
    private static final ResourceLocation RIGHT_WHEEL =
            ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/overlay/vehicle/land/right_wheel.png");
    private static final ResourceLocation ENGINE =
            ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/overlay/vehicle/land/engine.png");

    private static final Set<String> DRIVER_OVERLAY_VEHICLES = Set.of(
            "fcp:bmp1",
            "fcp:bmp1u",
            "fcp:bmp1p",
            "fcp:bmp1am",
            "fcp:bmp2",
            "fcp:puma",
            "fcp:puma_atgm",
            "fcp:marder1a3",
            "fcp:m2a2",
            "fcp:m2a2_busk",
            "fcp:m2a3",
            "fcp:m2a3_busk",
            "fcp:m6a2",
            "fcp:khrizantema",
            "fcp:bmp3",
            "fcp:bmp3_cage",
            "fcp:bmp3m",
            "fcp:brm3k",
            "fcp:bmp2d",
            "fcp:bmp2m",
            "fcp:bmp2md",
            "fcp:bmp2_noatgm",
            "fcp:btr82",
            "fcp:btr82at",
            "fcp:btr3e",
            "fcp:btr4mv1",
            "fcp:lav25",
            "fcp:lav25_mg",
            "fcp:stryker_m2",
            "fcp:stryker_mgs",
            "fcp:t72a",
            "fcp:t72av",
            "fcp:t72b",
            "fcp:t72b_1985",
            "fcp:t72b_1989",
            "fcp:t72b2",
            "fcp:t72b3",
            "fcp:t72b3_2016",
            "fcp:t72b3m",
            "fcp:t72m2_moderna",
            "fcp:t90a",
            "fcp:t90m",
            "fcp:t90m_2022",
            "fcp:t90m_2024",
            "fcp:t90ms",
            "fcp:aavp",
            "fcp:stryker_mk19",
            "fcp:stryker_tow",
            "fcp:stryker_dragoon",
            "fcp:stryker_mortar",
            "fcp:btr80",
            "fcp:btr80_cope",
            "fcp:btr82_cope",
            "fcp:brdm2",
            "fcp:brdm2_kornet",
            "fcp:m113",
            "fcp:m113a2",
            "fcp:skif",
            "fcp:msta",
            "fcp:2s31",
            "fcp:m109",
            "fcp:m1a1",
            "fcp:type10",
            "fcp:leopard2a4",
            "fcp:leopard2a4_ukr",
            "fcp:leopard2a6",
            "fcp:leopard2a7v",
            "fcp:m1a1_aim",
            "fcp:m1a1_ukr",
            "fcp:m1a2",
            "fcp:m1a2_tusk1",
            "fcp:m1a2_tusk2"
    );

    /**
     * Commanders who look out through a cupola's periscopes (no up/down): they get the driver frame too.
     * The cupola is the seat right after the gunner's, so it follows the turret controller between
     * single crew and multicrew data.
     */
    private static final Set<String> COMMANDER_CUPOLA_VEHICLES = Set.of(
            "fcp:m113",
            "fcp:skif",
            "fcp:marder1a3"
    );

    private static boolean isCupolaSeat(VehicleEntity ve, int seatIndex) {
        return COMMANDER_CUPOLA_VEHICLES.contains(EntityType.getKey(ve.getType()).toString())
                && seatIndex == ve.computed().getTurretControllerIndex() + 1;
    }

    /**
     * A troop seat's fixed periscope: a fixed camera with yaw and pitch locked (Min = Max) and no weapon.
     * These replaced the forced third-person passenger seats; they get the driver's periscope frame.
     */
    private static boolean isPeriscopeSeat(VehicleEntity ve, int seatIndex) {
        if (seatIndex <= 0 || seatIndex >= ve.computed().seats().size()) return false;
        if (seatIndex == ve.computed().getTurretControllerIndex()) return false;
        var seat = ve.computed().seats().get(seatIndex);
        return seat.getMinYaw() == seat.getMaxYaw() && seat.getMinPitch() == seat.getMaxPitch()
                && ve.getGunData(seatIndex) == null;
    }

    @Override
    public void render(Gui gui, GuiGraphics guiGraphics, float partialTick,
                       int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator() || mc.options.hideGui) return;
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return;

        var vehicle = player.getVehicle();
        if (!(vehicle instanceof VehicleEntity ve)) return;
        int seatIndex = ve.getSeatIndex(player);
        boolean periscope = isPeriscopeSeat(ve, seatIndex) || isCupolaSeat(ve, seatIndex);
        // Periscope troop seats also get the frame on @Empty vehicles (e.g. the Novator Unarmed); drivers only on @Land.
        String hud = ve.computed().getHudType();
        if (!hud.equals("@Land") && !(periscope && hud.equals("@Empty"))) return;
        if (!periscope) {
            if (seatIndex != 0) return;
            // A driver who also runs the turret (single crew) has their own sight; turretless vehicles (M113, Skif)
            // still report seat 0 as the controller, so only a seat with a gun counts.
            if (seatIndex == ve.computed().getTurretControllerIndex() && ve.getGunData(seatIndex) != null) return;

            String vehicleId = EntityType.getKey(ve.getType()).toString();
            if (!DRIVER_OVERLAY_VEHICLES.contains(vehicleId)) return;
        }

        int color = ve.getHudColor();
        var poseStack = guiGraphics.pose();

        // Set up proper alpha blending so semi-transparent vignette pixels are respected
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Frame rendering — preserves PNG aspect ratio across all resolutions:
        // - Wider than 16:9: pillarbox — fit to height, black bars left/right
        // - Taller than or equal to 16:9: zoom to fill height, crop sides
        float targetAspect = 16f / 9f;
        float screenAspect = (float) screenWidth / screenHeight;

        if (screenAspect > targetAspect) {
            float drawWidth = screenHeight * targetAspect;
            float drawX = (screenWidth - drawWidth) / 2f;
            // Fills overlap the image by 1px on each side to eliminate sub-pixel gaps
            int leftEnd = (int) Math.floor(drawX) + 1;
            int rightStart = (int) Math.ceil(drawX + drawWidth) - 1;
            guiGraphics.fill(0, 0, leftEnd, screenHeight, 0xFF000000);
            guiGraphics.fill(rightStart, 0, screenWidth, screenHeight, 0xFF000000);
            // Re-establish blend after fill() which resets render state
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderHelper.preciseBlit(guiGraphics, DRIVER_FRAME,
                    drawX, 0, 10f, 0f, 0f,
                    drawWidth, screenHeight, drawWidth, screenHeight);
        } else {
            // Zoom to fill height, crop sides symmetrically
            float zoomedWidth = screenHeight * targetAspect;
            float uOffset = (zoomedWidth - screenWidth) / 2f;
            RenderHelper.preciseBlit(guiGraphics, DRIVER_FRAME,
                    0, 0, 10f,
                    uOffset, 0f,
                    screenWidth, screenHeight,
                    zoomedWidth, screenHeight);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        // Compass
        RenderHelper.preciseBlitWithColor(guiGraphics, COMPASS,
                screenWidth / 2f - 128, 10f,
                128 + (64f / 45 * player.getYRot()), 0f,
                256f, 16f, 512f, 16f, color);

        // Roll indicator
        RenderHelper.preciseBlitWithColor(guiGraphics, ROLL_IND,
                screenWidth / 2f - 8, 30f,
                0f, 0f, 16f, 16f, 16f, 16f, color);

        // Speed
        guiGraphics.drawString(mc.font,
                Component.literal(FormatTool.format0D(ve.getAbsoluteSpeed() * 72, " KM/H")),
                screenWidth / 2 + 160, screenHeight / 2 - 48, color, false);

        // HP
        int bodyHeal = (int)(100 - (100 * ve.getHealth() / ve.getMaxHealth()));
        guiGraphics.drawString(mc.font,
                Component.literal(FormatTool.format0D(100 - bodyHeal, "")),
                screenWidth / 2 - 165, screenHeight / 2 - 46,
                MathTool.getGradientColor(color, 0xFF0000, bodyHeal, 2), false);

        // Smoke ready / reloading just below the HP - same call and spot as SBW's own land HUD.
        // Only for a seat that can actually fire it (see GunnerSmoke.canFireSmoke).
        if (GunnerSmoke.canFireSmoke(ve, player)) {
            DecoyOverlayHelper.renderThirdPersonDecoyInfo(ve, guiGraphics,
                    screenWidth / 2 - 165, screenHeight / 2 - 36, color);
        }

        // Component damage — hull fixed, turret line rotates
        RenderHelper.preciseBlitWithColor(guiGraphics, BODY,
                screenWidth / 2f + 96, screenHeight - 72f,
                0f, 0f, 32f, 32f, 32f, 32f,
                MathTool.getGradientColor(color, 0xFF0000, bodyHeal, 2));

        int leftWheelHeal = (int)(100 - (100 * ve.getLeftWheelHealth() / ve.getWheelMaxHealth()));
        RenderHelper.preciseBlitWithColor(guiGraphics, LEFT_WHEEL,
                screenWidth / 2f + 96, screenHeight - 72f,
                0f, 0f, 32f, 32f, 32f, 32f,
                MathTool.getGradientColor(color, 0xFF0000, leftWheelHeal, 2));

        int rightWheelHeal = (int)(100 - (100 * ve.getRightWheelHealth() / ve.getWheelMaxHealth()));
        RenderHelper.preciseBlitWithColor(guiGraphics, RIGHT_WHEEL,
                screenWidth / 2f + 96, screenHeight - 72f,
                0f, 0f, 32f, 32f, 32f, 32f,
                MathTool.getGradientColor(color, 0xFF0000, rightWheelHeal, 2));

        int engineHeal = (int)(100 - (100 * ve.getMainEngineHealth() / ve.getEngineMaxHealth()));
        RenderHelper.preciseBlitWithColor(guiGraphics, ENGINE,
                screenWidth / 2f + 96, screenHeight - 72f,
                0f, 0f, 32f, 32f, 32f, 32f,
                MathTool.getGradientColor(color, 0xFF0000, engineHeal, 2));

        // Turret direction line — rotates to show where gun points relative to fixed hull
        poseStack.pushPose();
        poseStack.rotateAround(
                Axis.ZP.rotationDegrees(
                        -Mth.lerp(partialTick, ve.getTurretYRotO(), ve.getTurretYRot())
                ),
                screenWidth / 2f + 112, screenHeight - 56f, 0f
        );
        int turretHeal = (int)(100 - (100 * ve.getTurretHealth() / ve.getTurretMaxHealth()));
        RenderHelper.preciseBlitWithColor(guiGraphics, LINE,
                screenWidth / 2f + 112, screenHeight - 71f,
                0f, 0f, 1f, 16f, 1f, 16f,
                MathTool.getGradientColor(color, 0xFF0000, turretHeal, 2));
        poseStack.popPose();
    }
}