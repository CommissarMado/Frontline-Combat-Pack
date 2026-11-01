package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.screens.MissilePosInputScreen;
import frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HIMARS coordinate input: an empty height means the vehicle's own height (SBW would use 0). The button belongs to the
 * open screen, so the height box is read from it rather than through the inner class's outer reference.
 */
@Mixin(MissilePosInputScreen.DoneButton.class)
public abstract class MissilePosInputDoneMixin {

    @Inject(method = "onPress", at = @At("HEAD"))
    private void fcp$defaultHeight(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof HimarsEntity himars)) return;
        if (!(mc.screen instanceof MissilePosInputScreen screen)) return;
        EditBox y = screen.getPosY();
        if (y.getValue().isBlank()) y.setValue(String.valueOf(himars.blockPosition().getY()));
    }
}
