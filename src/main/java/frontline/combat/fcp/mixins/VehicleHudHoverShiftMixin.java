package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.overlay.VehicleHudOverlay;
import frontline.combat.fcp.client.EngineHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * SBW draws the helicopter hover-mode text at x=85 in the health row, exactly where FCP's engine
 * element goes. On vehicles showing the engine element, the text moves right of it.
 */
@Mixin(value = VehicleHudOverlay.class, remap = false)
public abstract class VehicleHudHoverShiftMixin {

    @ModifyArg(
            method = "renderHoverInfo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
                    remap = true
            ),
            index = 2
    )
    private int fcp$shiftHoverText(int x) {
        return EngineHud.hoverTextX(x);
    }
}
