package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.client.overlay.RenderContext;
import com.atsuishio.superbwarfare.client.overlay.VehicleHudOverlay;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.client.EngineKeyHandler;
import frontline.combat.fcp.client.EngineHud;
import frontline.combat.fcp.entity.vehicle.engine.EngineControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Engine on/off bar in SBW's vehicle HUD, for vehicles with engine start/stop (EngineControl).
 *
 * Drawn in the health row, to the right of the health bar, with the same icon size, frame and bar
 * textures - only shorter. Full while the engine runs, empty otherwise. Injected just before SBW
 * draws the weapon info, while its HUD render state is still set up.
 */
@Mixin(value = VehicleHudOverlay.class, remap = false)
public abstract class VehicleHudEngineBarMixin {

    @Shadow @Final
    private static ResourceLocation VALUE_BAR;

    @Shadow @Final
    private static ResourceLocation VALUE_FRAME;

    @Shadow
    private int getArmorPlateCompatHeight(Player player) {
        throw new AssertionError();
    }

    private static final float FCP_ENGINE_BAR_WIDTH = 30f;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/client/overlay/VehicleHudOverlay;renderWeaponInfo(Lnet/minecraft/client/gui/GuiGraphics;Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;II)V"
            )
    )
    private void fcp$engineBar(RenderContext ctx, CallbackInfo ci) {
        Player player = ctx.getPlayer();
        if (player == null || !(player.getVehicle() instanceof VehicleEntity vehicle)) return;
        // Driver only: they're the one who can start and stop it.
        if (!EngineControl.appliesTo(vehicle) || vehicle.getFirstPassenger() != player) return;

        GuiGraphics g = ctx.getGuiGraphics();
        int y = ctx.getScreenHeight() - getArmorPlateCompatHeight(player);
        float x = EngineHud.X;
        byte state = EngineControl.state(vehicle);

        // Empty while off, fills over the start, full while running.
        float fill = switch (state) {
            case EngineControl.OFF -> 0f;
            case EngineControl.STARTING -> EngineKeyHandler.startProgress(vehicle, ctx.getPartialTick());
            default -> 1f;
        };

        // The bound key in place of an icon, vertically centred on the 6px bar.
        Font font = Minecraft.getInstance().font;
        String key = EngineHud.keyLabel();
        float barX = x + Math.max(8, font.width(key)) + 2;

        fcp$blitShort(g, VALUE_FRAME, barX, y - 12, 1f);
        if (fill > 0f) fcp$blitShort(g, VALUE_BAR, barX, y - 12, fill);
        // Text last: drawString changes the blend state, and the bars drawn after it lost their
        // transparency (black background).
        g.drawString(font, key, (int) x, y - 13, 0xFFFFFF, false);
        // Hand SBW back the blend it set up for the rest of its HUD.
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
    }

    /**
     * A shorter copy of a 60x6 HUD texture: its left and right halves, so the end caps stay crisp.
     * Only the first {@code fill} (0..1) of it is drawn.
     */
    private static void fcp$blitShort(GuiGraphics g, ResourceLocation texture, float x, float y, float fill) {
        float half = FCP_ENGINE_BAR_WIDTH / 2f;
        float width = (int) (FCP_ENGINE_BAR_WIDTH * fill);
        float left = Math.min(width, half), right = width - left;
        if (left > 0f) RenderHelper.preciseBlit(g, texture, x, y, 100f, 0f, 0f, left, 6f, 60f, 6f);
        if (right > 0f) RenderHelper.preciseBlit(g, texture, x + half, y, 100f, 60f - half, 0f, right, 6f, 60f, 6f);
    }
}
