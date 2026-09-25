package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.overlay.VehicleHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Swaps SBW's lightning-bolt energy icon for FCP's fuel icon on FCP vehicles.
 *
 * <p>SBW reads its {@code ENERGY} texture in exactly one place, the energy bar in
 * {@code render}, and draws it untinted at 8x8 with the whole texture stretched to fit - so any
 * square PNG works. SBW's own vehicles keep the stock icon.
 */
@Mixin(value = VehicleHudOverlay.class, remap = false)
public abstract class VehicleHudFuelIconMixin {

    @Shadow @Final
    private static ResourceLocation ENERGY;

    private static final ResourceLocation FCP_FUEL_ICON =
            new ResourceLocation("fcp", "textures/overlay/vehicle/base/fcp_fuel_icon.png");

    @Redirect(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/atsuishio/superbwarfare/client/overlay/VehicleHudOverlay;ENERGY:Lnet/minecraft/resources/ResourceLocation;",
                    opcode = 178 // GETSTATIC
            )
    )
    private ResourceLocation fcp$fuelIcon() {
        var player = Minecraft.getInstance().player;
        Entity vehicle = player == null ? null : player.getVehicle();
        if (vehicle != null && EntityType.getKey(vehicle.getType()).getNamespace().equals("fcp")) {
            return FCP_FUEL_ICON;
        }
        return ENERGY;
    }
}
