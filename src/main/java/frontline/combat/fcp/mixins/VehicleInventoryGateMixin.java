package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.FcpWorldSettings;
import frontline.combat.fcp.entity.vehicle.VehicleInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every route into SBW's own vehicle container ends in openMenu(). FCP's hold only opens on an
 * empty-handed shift-click, so shift-clicking with an item fell through to SBW and opened its
 * container instead; FCP vehicles now get their hold there too. The F8 inventory lock also stops
 * SBW's container on vehicles from other mods.
 */
@Mixin(value = VehicleEntity.class, remap = false)
public abstract class VehicleInventoryGateMixin {

    @Inject(method = "openMenu", at = @At("HEAD"), cancellable = true)
    private void fcp$gateInventory(Player player, CallbackInfo ci) {
        if (this instanceof VehicleInventory hold) {
            if (player instanceof ServerPlayer serverPlayer) hold.openVehicleInventory(serverPlayer);
            ci.cancel();
        } else if (FcpWorldSettings.refuseInventory(player)) {
            ci.cancel();
        }
    }
}
