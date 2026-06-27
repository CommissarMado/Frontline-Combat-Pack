package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hides the vanilla white hitbox (the F3+B AABB) for vehicles that use an OBB, so
 * only SBW's green OBB outline shows. The vanilla renderHitbox draws the white box
 * with a single LevelRenderer.renderLineBox call, then SBW injects at RETURN to draw
 * the OBB; this redirect skips just that white-box draw for OBB vehicles and leaves
 * SBW's OBB render (and every other entity's hitbox) untouched.
 *
 * Render-only: the AABB itself is unchanged, so collision and ground support are
 * exactly as before.
 */
@Mixin(EntityRenderDispatcher.class)
public class HideVehicleHitboxMixin {

    @Redirect(
            method = "renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;F)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLineBox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/AABB;FFFF)V"))
    private static void fcp$hideVehicleWhiteBox(PoseStack pose, VertexConsumer buffer, AABB box,
                                                float red, float green, float blue, float alpha,
                                                PoseStack pPoseStack, VertexConsumer pBuffer,
                                                Entity entity, float pPartialTicks) {
        // OBB vehicles: skip the white box (SBW still draws the OBB at RETURN).
        if (entity instanceof VehicleEntity vehicle && !vehicle.enableAABB()) {
            return;
        }
        LevelRenderer.renderLineBox(pose, buffer, box, red, green, blue, alpha);
    }
}