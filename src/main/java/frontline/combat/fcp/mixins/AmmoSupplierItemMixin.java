package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.item.ammo.AmmoSupplierItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops ammo items being consumed into SBW's player ammo pool on right-click.
 *
 * <p>AmmoSupplierItem.use() converts the held stack (or, with shift, every matching stack in the
 * inventory) into the abstract per-player ammo counter that the pool overlay showed. In FCP that
 * is actively harmful: emplacements are fed by CARRYING ammo items, so any stray right-click
 * silently destroyed the ammo the gun needed - including the click that should have mounted the
 * emplacement, which is why mounting with ammo in hand ate the stack and refused the seat.
 *
 * <p>Cancelling at HEAD returns PASS, leaving the stack untouched and letting the click fall
 * through to whatever is being right-clicked (mounting an emplacement, placing, etc).
 *
 * <p>Ammo still works everywhere it matters: guns and vehicles read ammo ITEMS from the
 * inventory. Only the item -> pool conversion is disabled.
 */
@Mixin(value = AmmoSupplierItem.class, remap = false)
public abstract class AmmoSupplierItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void fcp$noAmmoPoolConversion(Level level, Player player, InteractionHand hand,
                                          CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        cir.setReturnValue(InteractionResultHolder.pass(player.getItemInHand(hand)));
    }
}