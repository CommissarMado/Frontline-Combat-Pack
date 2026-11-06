package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.renderer.special.OBBRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frontline.combat.fcp.entity.vehicle.GroundContact;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * F3+B: SBW draws each hitbox at its offset from the entity, on top of the entity's interpolated position, so a shift that
 * changes per tick shows as a jump. Interpolates it like the model. The collision box (Vec3.y ordinal 1) has no such shift.
 */
@Mixin(OBBRenderer.class)
public abstract class OBBRendererGroundMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;y()D", ordinal = 0))
    private double fcp$interactiveY(Vec3 pos, VehicleEntity vehicle, List<?> obbs, PoseStack pose, VertexConsumer lines, float a, float b, float c, float d, float e) {
        return fcp$drawnFrom(pos, vehicle);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;y()D", ordinal = 2))
    private double fcp$partY(Vec3 pos, VehicleEntity vehicle, List<?> obbs, PoseStack pose, VertexConsumer lines, float a, float b, float c, float d, float e) {
        return fcp$drawnFrom(pos, vehicle);
    }

    @Unique
    private static double fcp$drawnFrom(Vec3 pos, VehicleEntity vehicle) {
        GroundContact contact = (GroundContact) vehicle;
        return pos.y + contact.fcp$groundOffset(1f) - contact.fcp$groundOffset(Minecraft.getInstance().getFrameTime());
    }
}
