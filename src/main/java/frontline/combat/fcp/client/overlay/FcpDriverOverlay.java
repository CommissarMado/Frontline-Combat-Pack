package frontline.combat.fcp.client.overlay;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class FcpDriverOverlay implements IGuiOverlay {

    public static final FcpDriverOverlay INSTANCE = new FcpDriverOverlay();
    public static final String ID = "fcp_driver_hud";

    private static final ResourceLocation DRIVER_FRAME =
            new ResourceLocation("fcp", "textures/overlay/vehicle/frame/driver.png");
    private static final ResourceLocation COMPASS =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/base/compass.png");
    private static final ResourceLocation ROLL_IND =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/roll_ind.png");
    private static final ResourceLocation LINE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/line.png");
    private static final ResourceLocation BODY =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/body.png");
    private static final ResourceLocation LEFT_WHEEL =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/left_wheel.png");
    private static final ResourceLocation RIGHT_WHEEL =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/right_wheel.png");
    private static final ResourceLocation ENGINE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/engine.png");

    private static final Set<String> DRIVER_OVERLAY_VEHICLES = Set.of(
            "fcp:bmp1",
            "fcp:bmp1u",
            "fcp:bmp2",
            "fcp:btr82",
            "fcp:lav25",
            "fcp:stryker_m2",
            "fcp:stryker_mgs",
            "fcp:t72av",
            "fcp:aavp",
            "fcp:stryker_mk19",
            "fcp:stryker_tow",
            "fcp:stryker_dragoon",
            "fcp:stryker_mortar",
            "fcp:btr80"
    );

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick,
                       int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator() || mc.options.hideGui) return;
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return;

        var vehicle = player.getVehicle();
        if (!(vehicle instanceof VehicleEntity ve)) return;
        if (!ve.computed().getHudType().equals("@Land")) return;

        int seatIndex = ve.getSeatIndex(player);
        if (seatIndex != 0) return;
        if (seatIndex == ve.computed().getTurretControllerIndex()) return;

        String vehicleId = EntityType.getKey(ve.getType()).toString();
        if (!DRIVER_OVERLAY_VEHICLES.contains(vehicleId)) return;

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
        RenderHelper.blit(poseStack, COMPASS,
                screenWidth / 2f - 128, 10f,
                128 + (64f / 45 * player.getYRot()), 0f,
                256f, 16f, 512f, 16f, color);

        // Roll indicator
        RenderHelper.blit(poseStack, ROLL_IND,
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

        // Component damage — hull fixed, turret line rotates
        RenderHelper.blit(poseStack, BODY,
                screenWidth / 2f + 96, screenHeight - 72f,
                0f, 0f, 32f, 32f, 32f, 32f,
                MathTool.getGradientColor(color, 0xFF0000, bodyHeal, 2));

        int leftWheelHeal = (int)(100 - (100 * ve.getLeftWheelHealth() / ve.getWheelMaxHealth()));
        RenderHelper.blit(poseStack, LEFT_WHEEL,
                screenWidth / 2f + 96, screenHeight - 72f,
                0f, 0f, 32f, 32f, 32f, 32f,
                MathTool.getGradientColor(color, 0xFF0000, leftWheelHeal, 2));

        int rightWheelHeal = (int)(100 - (100 * ve.getRightWheelHealth() / ve.getWheelMaxHealth()));
        RenderHelper.blit(poseStack, RIGHT_WHEEL,
                screenWidth / 2f + 96, screenHeight - 72f,
                0f, 0f, 32f, 32f, 32f, 32f,
                MathTool.getGradientColor(color, 0xFF0000, rightWheelHeal, 2));

        int engineHeal = (int)(100 - (100 * ve.getMainEngineHealth() / ve.getEngineMaxHealth()));
        RenderHelper.blit(poseStack, ENGINE,
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
        RenderHelper.blit(poseStack, LINE,
                screenWidth / 2f + 112, screenHeight - 71f,
                0f, 0f, 1f, 16f, 1f, 16f,
                MathTool.getGradientColor(color, 0xFF0000, turretHeal, 2));
        poseStack.popPose();
    }
}