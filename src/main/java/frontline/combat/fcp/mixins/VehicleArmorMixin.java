package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.balance.VehicleArmor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vehicles with an FCP armour profile take Squad-model damage instead of SBW's modifiers (see
 * VehicleArmor). Also: SBW ignores non-explosive damage from a vehicle's last driver, so a vehicle
 * you once drove couldn't be shot by you afterwards (while explosions still hit). That protection now
 * only holds while the attacker is actually aboard.
 */
@Mixin(VehicleEntity.class)
public abstract class VehicleArmorMixin {

    @Inject(method = "hurt", at = @At("HEAD"))
    private void fcp$forgetAbsentDriver(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        Entity attacker = source.getEntity();
        if (attacker != null && attacker == self.getLastDriver() && attacker.getVehicle() != self) {
            self.setLastDriverUUID("undefined");
        }
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", remap = false,
            target = "Lcom/atsuishio/superbwarfare/entity/vehicle/damage/DamageModifier;compute(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float fcp$squadDamage(DamageModifier modifier, Entity entity, DamageSource source, float amount) {
        float sbw = modifier.compute(entity, source, amount);
        return VehicleArmor.damage((VehicleEntity) (Object) this, source, amount, sbw);
    }
}
