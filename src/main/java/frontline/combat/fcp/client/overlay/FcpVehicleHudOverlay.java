package frontline.combat.fcp.client.overlay;

import com.atsuishio.superbwarfare.data.vehicle.VehicleData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class FcpVehicleHudOverlay implements IGuiOverlay {

    public static final FcpVehicleHudOverlay INSTANCE = new FcpVehicleHudOverlay();
    public static final String ID = "frontlinecombatpack_vehicle_hud";

    // Add your overlay textures here
    private static final ResourceLocation LAV_HUD =
            new ResourceLocation("frontlinecombatpack", "textures/overlay/vehicle/lav_hud.png");

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick,
                       int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator()) return;
        if (mc.options.hideGui) return;

        var vehicle = player.getVehicle();
        if (!(vehicle instanceof VehicleEntity ve)) return;

        // Read the HudType from vehicle data — your vehicles will have "HudType": "@FcpLav" etc.
        String hudType = VehicleData.compute(ve).getHudType();
        if (!hudType.startsWith("@Fcp")) return; // only render for our vehicles

        int seatIndex = ve.getSeatIndex(player);
        // Only render for the appropriate seat (e.g. turret controller)
        if (seatIndex != ve.computed().getTurretControllerIndex()) return;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Dispatch to per-vehicle HUD renderer based on HudType
        switch (hudType) {
            case "@FcpLav" -> renderLavHud(guiGraphics, ve, screenWidth, screenHeight, partialTick);
            case "@FcpBmp" -> renderBmpHud(guiGraphics, ve, screenWidth, screenHeight, partialTick);
            // add more as needed
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private void renderLavHud(GuiGraphics g, VehicleEntity ve, int w, int h, float pt) {
        // Draw your full-screen overlay texture
        g.blit(LAV_HUD, 0, 0, 0, 0, w, h, w, h);
        // Add additional elements (compass, health, ammo, etc.)
    }

    private void renderBmpHud(GuiGraphics g, VehicleEntity ve, int w, int h, float pt) {
        // different HUD for BMP, etc.
    }
}