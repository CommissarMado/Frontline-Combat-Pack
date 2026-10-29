package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.screens.MissilePosInputScreen;
import frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HIMARS coordinate input: the height box starts empty, with the vehicle's own height as its hint, instead of
 * SBW's re-fill of the last target's height; MissilePosInputDoneMixin then uses the vehicle's height for an empty box.
 * Other vehicles keep SBW's behaviour.
 */
@Mixin(MissilePosInputScreen.class)
public abstract class MissilePosInputScreenMixin {

    @Shadow(remap = false)
    private boolean init;

    @Shadow(remap = false)
    public abstract EditBox getPosY();

    @Unique
    private boolean fcp$heightReset;

    @Inject(method = "tick", at = @At("TAIL"))
    private void fcp$himarsHeight(CallbackInfo ci) {
        if (fcp$heightReset || !init) return;
        fcp$heightReset = true;
        var player = Minecraft.getInstance().player;
        if (player == null || !(player.getVehicle() instanceof HimarsEntity himars)) return;
        EditBox y = getPosY();
        y.setValue("");
        y.setHint(Component.literal(String.valueOf(himars.blockPosition().getY())));
    }
}
