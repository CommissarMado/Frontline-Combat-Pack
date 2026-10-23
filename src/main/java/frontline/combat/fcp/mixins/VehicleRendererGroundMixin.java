package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import frontline.combat.fcp.entity.vehicle.GroundContact;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the model with the same ground-contact shift as the vehicle transform (GeckoLib renderer). */
@Mixin(VehicleRenderer.class)
public abstract class VehicleRendererGroundMixin {

    @Inject(method = "vehicleAxis", at = @At("HEAD"), remap = false)
    private void fcp$groundShift(VehicleEntity entity, PoseStack poseStack, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (entity instanceof GroundContact contact) poseStack.translate(0, contact.fcp$groundOffset(partialTicks), 0);
    }
}
