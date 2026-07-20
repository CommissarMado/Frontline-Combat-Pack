package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.client.overlay.VehicleMainWeaponHudOverlay;
import com.atsuishio.superbwarfare.client.overlay.weapon.LandVehicleHud;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
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
            "fcp:t72av",
            "fcp:aavp",
            "fcp:matv_tow",
            "fcp:stryker_mortar",
            "fcp:ural_grad",
            "fcp:toyota_hilux_bmp",
            "fcp:toyota_hilux_spg9",
            "fcp:uaz_spg9",
            "fcp:toyota_hilux_mortar"
    );

    // Vehicles that suppress the TV frame entirely but show no replacement overlay
    private static final Set<String> FCP_NO_FRAME_VEHICLES = Set.of(
            "fcp:toyota_hilux_zu23"
    );

    // Vehicles whose gunner seat gets the mortar-style Pitch / Yaw / Range readout.
    // Muzzle velocity and projectile gravity are pulled from that seat's own GunData,
    // so it stays correct per vehicle/weapon with no hardcoded constants.
    //
    // Additive: does NOT by itself suppress the TV frame or draw gunner.png. Add the
    // vehicle to FCP_NO_FRAME_VEHICLES / FCP_GUNNER_FRAME_VEHICLES too if you want those.
    private static final Set<String> FCP_MORTAR_HUD_VEHICLES = Set.of(
            "fcp:ural_grad",
            "fcp:stryker_mortar",
            "fcp:toyota_hilux_rocket_pod",
            "fcp:toyota_hilux_mortar"
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
        boolean hasMortarHud = FCP_MORTAR_HUD_VEHICLES.contains(vehicleId);

        if (!hasGunnerFrame && !noFrame && !hasMortarHud) return;

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

            // Mortar-style ballistic readout: Pitch / Yaw / Range
            if (hasMortarHud) {
                renderMortarInfo(vehicle, player, guiGraphics, mc, partialTick,
                        screenWidth, screenHeight, color);
            }

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

    /**
     * Mortar-style Pitch / Yaw / Range readout.
     *
     * The range is solved to GROUND level, not to launch height. SBW's own
     * RangeTool.getRange() assumes the shell returns to the height it was fired from,
     * but the muzzle sits several blocks above the ground (and climbs further as the
     * barrel elevates), so the shell keeps falling past that point and lands long.
     * On the Grad that is roughly +6% at minimum elevation.
     *
     *   h = muzzle height above the ground the vehicle is standing on
     *   t = (vy + sqrt(vy^2 + 2*g*h)) / g     <- positive root = the descending branch
     *   R = vx * t
     *
     * Layout mirrors SBW's MortarInfoOverlay: three lines from (centre - 90, centre - 26).
     */
    private void renderMortarInfo(
            VehicleEntity vehicle,
            Player player,
            GuiGraphics guiGraphics,
            Minecraft mc,
            float partialTick,
            int screenWidth,
            int screenHeight,
            int color
    ) {
        // Actual launch vector (resolves the seat's GunData -> ShootPos.Directions), so
        // this stays true even if a weapon's tubes are angled relative to the barrel.
        // getXRotFromVector is atan2(y, horizontal): positive when elevated = theta.
        Vec3 shootVec = vehicle.getShootVec(player, partialTick);
        double pitch = VehicleVecUtils.getXRotFromVector(shootVec);
        double yaw = -VehicleVecUtils.getYRotFromVector(shootVec);

        double velocity = vehicle.getProjectileVelocity(player);
        double gravity = vehicle.getProjectileGravity(player);

        String rangeStr;
        if (gravity <= 0 || velocity <= 0 || pitch <= 0) {
            // Flat-trajectory weapon, or barrel level/depressed: no ballistic arc to solve
            rangeStr = "---m";
        } else {
            Vec3 muzzle = vehicle.getShootPos(player, partialTick);
            double h = Math.max(0.0, muzzle.y - vehicle.getY());

            double rad = Math.toRadians(pitch);
            double vy = velocity * Math.sin(rad);
            double vx = velocity * Math.cos(rad);

            double flightTime = (vy + Math.sqrt(vy * vy + 2 * gravity * h)) / gravity;
            double range = vx * flightTime;
            rangeStr = FormatTool.format1D(Math.floor(range), "m");
        }

        int baseX = screenWidth / 2 - 90;
        int baseY = screenHeight / 2 - 26;

        guiGraphics.drawString(mc.font,
                Component.translatable("tips.superbwarfare.mortar.pitch")
                        .append(Component.literal(FormatTool.format1D(pitch, "\u00B0"))),
                baseX, baseY, color, false);

        guiGraphics.drawString(mc.font,
                Component.translatable("tips.superbwarfare.mortar.yaw")
                        .append(Component.literal(FormatTool.format1D(yaw, "\u00B0"))),
                baseX, baseY + 10, color, false);

        guiGraphics.drawString(mc.font,
                Component.translatable("tips.superbwarfare.mortar.range")
                        .append(Component.literal(rangeStr)),
                baseX, baseY + 20, color, false);
    }
}