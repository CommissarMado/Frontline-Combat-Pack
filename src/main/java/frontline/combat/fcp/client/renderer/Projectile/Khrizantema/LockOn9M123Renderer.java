package frontline.combat.fcp.client.renderer.Projectile.Khrizantema;

import com.atsuishio.superbwarfare.entity.projectile.WireGuideMissileEntity;
import com.atsuishio.superbwarfare.init.ModEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import frontline.combat.fcp.entity.projectile.Khrizantema.LockOn9M123Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * The radar-guided 9M123 (LockOn9M123Entity), drawn exactly like SBW's wire-guided missile.
 * SBW draws that with its own bedrock-model renderer, which only accepts its own projectile classes, so
 * a client-only stand-in WireGuideMissileEntity is posed like this missile and handed to SBW's renderer.
 */
public class LockOn9M123Renderer extends EntityRenderer<LockOn9M123Entity> {

    private WireGuideMissileEntity standIn;

    public LockOn9M123Renderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(@NotNull LockOn9M123Entity missile, float yaw, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        if (standIn == null || standIn.level() != missile.level()) {
            standIn = new WireGuideMissileEntity((EntityType<WireGuideMissileEntity>) ModEntities.WIRE_GUIDE_MISSILE.get(), missile.level());
        }
        standIn.setPos(missile.getX(), missile.getY(), missile.getZ());
        standIn.setXRot(missile.getXRot());
        standIn.setYRot(missile.getYRot());
        standIn.xRotO = missile.xRotO;
        standIn.yRotO = missile.yRotO;
        standIn.tickCount = missile.tickCount;
        standIn.setSyncedTick(missile.tickCount);
        entityRenderDispatcher.getRenderer(standIn).render(standIn, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull LockOn9M123Entity missile) {
        return new ResourceLocation("superbwarfare", "textures/bedrock/projectile/wire_guide_missile.png");
    }
}
