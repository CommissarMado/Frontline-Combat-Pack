package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.client.overlay.VehicleMainWeaponHudOverlay;
import com.atsuishio.superbwarfare.client.overlay.weapon.LandVehicleHud;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.tools.FormatTool;
import com.atsuishio.superbwarfare.tools.MathTool;
import com.atsuishio.superbwarfare.tools.TraceTool;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = LandVehicleHud.class, remap = false)
public class LandVehicleHudStatsMixin {

    // Vehicles that replace the TV frame with gunner.png
    private static final Set<String> FCP_GUNNER_FRAME_VEHICLES = Set.of(
            "fcp:bmp1",
            "fcp:bmp2",
            "fcp:btr82",
            "fcp:lav25",
            "fcp:stryker_mgs",
            "fcp:t72av"
    );

    // Vehicles that suppress the TV frame entirely but show no replacement overlay
    private static final Set<String> FCP_NO_FRAME_VEHICLES = Set.of(
            "fcp:matv"
    );

    private static final ResourceLocation GUNNER_FRAME =
            new ResourceLocation("fcp", "textures/overlay/vehicle/frame/gunner.png");
    private static final ResourceLocation COMPASS =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/base/compass.png");
    private static final ResourceLocation LINE =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/line.png");
    private static final ResourceLocation ROLL_IND =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/helicopter/roll_ind.png");
    private static final ResourceLocation BODY =
            new ResourceLocation("superbwarfare", "textures/overlay/vehicle/land/body.png");

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void replaceLandHudForFcp(
            VehicleEntity vehicle,
            Player player,
            ForgeGui gui,
            GuiGraphics guiGraphics,
            float partialTick,
            int screenWidth,
            int screenHeight,
            CallbackInfo ci
    ) {
        String vehicleId = EntityType.getKey(vehicle.getType()).toString();
        boolean hasGunnerFrame = FCP_GUNNER_FRAME_VEHICLES.contains(vehicleId);
        boolean noFrame = FCP_NO_FRAME_VEHICLES.contains(vehicleId);

        if (!hasGunnerFrame && !noFrame) return;

        ci.cancel();

        // Only render for the turret controller seat
        if (vehicle.getSeatIndex(player) != vehicle.computed().getTurretControllerIndex()) return;

        Minecraft mc = gui.getMinecraft();
        int color = vehicle.getHudColor();
        var poseStack = guiGraphics.pose();

        // Set up proper alpha blending so semi-transparent vignette pixels are respected
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

        if (mc.options.getCameraType() == CameraType.FIRST_PERSON || ClientEventHandler.zoomVehicle) {

            if (hasGunnerFrame) {
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
                    RenderHelper.preciseBlit(guiGraphics, GUNNER_FRAME,
                            drawX, 0, 10f, 0f, 0f,
                            drawWidth, screenHeight, drawWidth, screenHeight);
                } else {
                    // Zoom to fill height, crop sides symmetrically
                    float zoomedWidth = screenHeight * targetAspect;
                    float uOffset = (zoomedWidth - screenWidth) / 2f;
                    RenderHelper.preciseBlit(guiGraphics, GUNNER_FRAME,
                            0, 0, 10f,
                            uOffset, 0f,
                            screenWidth, screenHeight,
                            zoomedWidth, screenHeight);
                }
            }

            // Line below crosshair
            RenderHelper.blit(poseStack, LINE,
                    screenWidth / 2f - 64, screenHeight - 56f,
                    0f, 0f, 128f, 1f, 128f, 1f, color);

            // Compass
            RenderHelper.blit(poseStack, COMPASS,
                    screenWidth / 2f - 128, 10f,
                    128 + (64f / 45 * player.getYRot()), 0f,
                    256f, 16f, 512f, 16f, color);

            // Roll indicator
            RenderHelper.blit(poseStack, ROLL_IND,
                    screenWidth / 2f - 8, 30f,
                    0f, 0f, 16f, 16f, 16f, 16f, color);

            // Turret health bar
            int turretHeal = (int)(100 - (100 * vehicle.getTurretHealth() / vehicle.getTurretMaxHealth()));
            RenderHelper.blit(poseStack, LINE,
                    screenWidth / 2f + 112, screenHeight - 71f,
                    0f, 0f, 1f, 16f, 1f, 16f,
                    MathTool.getGradientColor(color, 0xFF0000, turretHeal, 2));

            // Hull direction indicator
            poseStack.pushPose();
            poseStack.rotateAround(
                    Axis.ZP.rotationDegrees(
                            Mth.lerp(partialTick, vehicle.getTurretYRotO(), vehicle.getTurretYRot())
                    ),
                    screenWidth / 2f + 112, screenHeight - 56f, 0f
            );
            int bodyHeal = (int)(100 - (100 * vehicle.getHealth() / vehicle.getMaxHealth()));
            RenderHelper.blit(poseStack, BODY,
                    screenWidth / 2f + 96, screenHeight - 72f,
                    0f, 0f, 32f, 32f, 32f, 32f,
                    MathTool.getGradientColor(color, 0xFF0000, bodyHeal, 2));
            poseStack.popPose();

            // Rangefinder
            var camera = mc.gameRenderer.getMainCamera();
            var cameraPos = camera.getPosition();
            var viewVec = new Vec3(camera.getLookVector());

            var result = player.level().clip(new ClipContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(player.getViewVector(1f).scale(512.0)),
                    ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
            double blockRange = player.getEyePosition(1f).distanceTo(result.getLocation());

            var lookingEntity = TraceTool.camerafFindLookingEntity(player, cameraPos, viewVec, 512.0);
            String rangeStr;
            if (lookingEntity != null) {
                rangeStr = FormatTool.format0D(player.distanceTo(lookingEntity), " m");
            } else if (blockRange > 500) {
                rangeStr = "---m";
            } else {
                rangeStr = FormatTool.format0D(blockRange, " m");
            }
            int rangeWidth = mc.font.width(rangeStr);
            guiGraphics.drawString(mc.font, Component.literal(rangeStr),
                    screenWidth / 2 - rangeWidth / 2, screenHeight - 53, color, false);

            // Weapon info
            var gunData = vehicle.getGunData(player);
            if (gunData != null) {
                VehicleMainWeaponHudOverlay.renderWeaponInfoFirst(
                        guiGraphics, vehicle, player, gunData,
                        mc.font, screenWidth, screenHeight, color);
            }

            // Energy warning
            VehicleMainWeaponHudOverlay.renderEnergyInfo(
                    vehicle, guiGraphics, screenWidth, screenHeight, mc.font);
        }
    }
}