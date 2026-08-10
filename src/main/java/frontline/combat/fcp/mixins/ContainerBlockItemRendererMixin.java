package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.renderer.item.ContainerBlockItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import frontline.combat.fcp.client.FcpContainerIcon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the 3D "container" crate in GUIs for FCP vehicles that have a custom icon, so
 * {@link frontline.combat.fcp.client.FcpContainerIconDecorator} can draw the icon in its place.
 * require = 0: if this can't apply on some SBW build, the mod still loads (icon just draws over
 * the crate instead of replacing it). Only FCP vehicles are affected; SBW containers are untouched.
 */
@Mixin(ContainerBlockItemRenderer.class)
public class ContainerBlockItemRendererMixin {

    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void fcp$hideCrateForFcpIcon(ItemStack stack, ItemDisplayContext transformType,
                                         PoseStack poseStack, MultiBufferSource bufferSource,
                                         int packedLight, int packedOverlay, CallbackInfo ci) {
        if (transformType == ItemDisplayContext.GUI && FcpContainerIcon.getIcon(stack) != null) {
            ci.cancel();
        }
    }
}
