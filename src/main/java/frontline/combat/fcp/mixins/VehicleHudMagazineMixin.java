package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.client.overlay.VehicleHudOverlay;
import com.atsuishio.superbwarfare.data.gun.GunData;
import frontline.combat.fcp.entity.vehicle.CamoEmplacementEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Draws an emplacement's "loaded / reserve" pair at one uniform size, vertically centred.
 *
 * <p>Stock SBW renders the loaded count large (scale 0.25) and the reserve small (0.125) on a
 * lower baseline. For a crew-served gun the two read better matched, so both are drawn at
 * {@link #FCP_SCALE} on a shared centre line.
 *
 * <p>The VALUES are left completely alone: the reserve is still SBW's own
 * {@code countBackupAmmo(vehicle)} — which resolves to the gunner's carried ammo because
 * CamoEmplacementEntity exposes the gunner's inventory as its ITEM_HANDLER capability — so "99+"
 * for large reserves and the infinity glyph for creative both behave exactly as on every other
 * vehicle.
 */
@Mixin(value = VehicleHudOverlay.class, remap = false)
public abstract class VehicleHudMagazineMixin {

    /** One scale for both numbers. Slightly under SBW's large 0.25. */
    private static final float FCP_SCALE = 0.2f;
    private static final float FCP_SBW_LARGE = 0.25f;
    private static final float FCP_SBW_SMALL = 0.125f;

    /** True while rendering an FCP emplacement. */
    private static boolean fcp$emplacement = false;

    /**
     * Digits drawn in the reserve slot this frame, 0 if none (creative shows an infinity SPRITE
     * via a blit, not renderNumber - so no digits, and no extra width to compensate for).
     * Reset every frame; the reserve always renders before the fraction bar.
     */
    private static int fcp$reserveDigits = 0;


    /**
     * Digits in the reserve slot. SBW caps that slot at "99+", so this is only ever 1 or 2 -
     * no need to count arbitrary lengths.
     */
    private static int fcp$digits(int value) {
        return value >= 10 ? 2 : 1;
    }

    private static float fcp$digitWidth(float scale) {
        return 20f * scale;
    }

    private static float fcp$glyphHeight(float scale) {
        return 30f * scale;
    }

    /**
     * SBW draws the small slot (reserve + fraction bar) this far BELOW the large slot.
     * Subtracting it converts a small-slot y into the equivalent large-slot y, so everything can
     * be centred on one line instead of each element centring on its own.
     */
    private static final float FCP_SMALL_SLOT_DROP = 3.5f;

    /**
     * Shared centre line for the whole group, taken from where SBW puts the LARGE number, so the
     * pair sits where the loaded count already sat rather than drifting up or down the frame.
     *
     * @param sbwY     the y SBW passed for this element
     * @param smallSlot true for the reserve/fraction, which SBW positions FCP_SMALL_SLOT_DROP lower
     */
    private static float fcp$centredY(float sbwY, boolean smallSlot) {
        float largeSlotY = smallSlot ? sbwY - FCP_SMALL_SLOT_DROP : sbwY;
        float centre = largeSlotY + fcp$glyphHeight(FCP_SBW_LARGE) / 2f;
        return centre - fcp$glyphHeight(FCP_SCALE) / 2f;
    }

    /** Flag whether this frame is an emplacement; the value itself is passed straight through. */
    @Redirect(
            method = "renderWeaponInfo",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/data/gun/GunData;countBackupAmmo(Lnet/minecraft/world/entity/Entity;)I"
            )
    )
    private int fcp$markEmplacement(GunData data, Entity source) {
        fcp$emplacement = source instanceof CamoEmplacementEntity;
        fcp$reserveDigits = 0;
        return data.countBackupAmmo(source);
    }

    /** Reserve, capped "99" branch. */
    @Redirect(
            method = "renderWeaponInfo",
            at = @At(value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/client/overlay/VehicleHudOverlay;renderNumber(Lnet/minecraft/client/gui/GuiGraphics;IZFFF)V",
                    ordinal = 0)
    )
    private void fcp$reserveCapped(VehicleHudOverlay overlay, GuiGraphics g, int number,
                                   boolean percent, float x, float y, float scale) {
        fcp$draw(g, number, percent, x, y, scale, true);
    }

    /** Reserve, normal branch. */
    @Redirect(
            method = "renderWeaponInfo",
            at = @At(value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/client/overlay/VehicleHudOverlay;renderNumber(Lnet/minecraft/client/gui/GuiGraphics;IZFFF)V",
                    ordinal = 1)
    )
    private void fcp$reserveNormal(VehicleHudOverlay overlay, GuiGraphics g, int number,
                                   boolean percent, float x, float y, float scale) {
        fcp$draw(g, number, percent, x, y, scale, true);
    }

    /** Loaded count. */
    @Redirect(
            method = "renderWeaponInfo",
            at = @At(value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/client/overlay/VehicleHudOverlay;renderNumber(Lnet/minecraft/client/gui/GuiGraphics;IZFFF)V",
                    ordinal = 2)
    )
    private void fcp$loaded(VehicleHudOverlay overlay, GuiGraphics g, int number,
                            boolean percent, float x, float y, float scale) {
        fcp$draw(g, number, percent, x, y, scale, false);
    }

    /**
     * Draw at the uniform scale, keeping the right edge where SBW had it (digits are
     * right-aligned, so a wider glyph would otherwise push past the frame) and re-centring
     * vertically. Non-emplacements fall through untouched.
     */
    private void fcp$draw(GuiGraphics g, int number, boolean percent,
                          float x, float y, float sbwScale, boolean smallSlot) {
        if (!fcp$emplacement) {
            fcp$renderNumber(g, number, percent, x, y, sbwScale);
            return;
        }
        if (smallSlot) fcp$reserveDigits = fcp$digits(number);
        // Only ever pull LEFT (when our glyph is wider than SBW's). A negative fix would shove the
        // loaded count to the right, into the fraction bar - which is what caused the overlap.
        float rightEdgeFix = Math.max(0f, fcp$digitWidth(FCP_SCALE) - fcp$digitWidth(sbwScale));
        // Creative shows the infinity sprite instead of digits; nudge the loaded count to suit.
        float infiniteFix = (!smallSlot && fcp$reserveDigits <= 0) ? FCP_LOADED_SHIFT_INFINITE : 0f;
        fcp$renderNumber(g, number, percent, x - rightEdgeFix + infiniteFix,
                fcp$centredY(y, smallSlot), FCP_SCALE);
    }

    /**
     * Horizontal spacing for the fraction bar - its Y is left exactly as SBW draws it.
     *
     * SBW positions the bar for ITS digit width (2.5px); ours are 4px, so the reserve reaches
     * further left and the bar ends up crowding it. Only three cases exist, since SBW caps the
     * slot at "99+": one digit, two digits, or the creative infinity sprite - a blit of unchanged
     * width, which must not move at all. The two digit values are the constants below.
     */
    /**
     * The loaded count shifts right by this much when the reserve is the infinity sprite.
     * That sprite is a fixed-width blit, so the gap between it and the loaded count differs from
     * the digit cases and needs its own nudge.
     */
    private static final float FCP_LOADED_SHIFT_INFINITE = 1.5f;

    /** Shift for a one-digit reserve. */
    private static final float FCP_SHIFT_ONE_DIGIT = -1.5f;
    /** Shift for the two-digit / "99+" reserve, tuned by eye rather than by glyph width. */
    private static final float FCP_SHIFT_TWO_DIGIT = -2.5f;

    private static float fcp$fractionShift() {
        if (fcp$reserveDigits <= 0) return 0f;                       // infinity sprite: don't move
        return fcp$reserveDigits >= 2 ? FCP_SHIFT_TWO_DIGIT : FCP_SHIFT_ONE_DIGIT;
    }

    @Redirect(
            method = "renderWeaponInfo",
            at = @At(value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/client/RenderHelper;preciseBlit(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;FFFFFFFF)V")
    )
    private void fcp$spaceFraction(GuiGraphics gui, ResourceLocation texture, float x, float y,
                                   float u, float v, float width, float height,
                                   float texWidth, float texHeight) {
        float drawX = x;
        if (fcp$emplacement && texture.getPath().endsWith("fraction.png")) {
            drawX = x + fcp$fractionShift();
        }
        RenderHelper.preciseBlit(gui, texture, drawX, y, u, v, width, height, texWidth, texHeight);
    }

    /** Local copy of SBW's digit renderer (its own is private). Digits are right-aligned from x. */
    private static void fcp$renderNumber(GuiGraphics guiGraphics, int number, boolean percent,
                                         float x, float y, float scale) {
        ResourceLocation numbers = new ResourceLocation("superbwarfare",
                "textures/overlay/vehicle/weapon/frame/number.png");
        float pX = x;
        if (percent) {
            pX -= 32 * scale;
            RenderHelper.preciseBlit(guiGraphics, numbers, pX + 20 * scale, y, 100f,
                    200 * scale, 0f, 32 * scale, 30 * scale, 300 * scale, 30 * scale);
        }
        if (number == 0) {
            RenderHelper.preciseBlit(guiGraphics, numbers, pX, y, 100f,
                    0f, 0f, 20 * scale, 30 * scale, 300 * scale, 30 * scale);
        }
        int index = 0;
        while (number > 0) {
            int digit = number % 10;
            RenderHelper.preciseBlit(guiGraphics, numbers, pX - index * 20 * scale, y, 100f,
                    digit * 20 * scale, 0f, 20 * scale, 30 * scale, 300 * scale, 30 * scale);
            number /= 10;
            index++;
        }
    }
}