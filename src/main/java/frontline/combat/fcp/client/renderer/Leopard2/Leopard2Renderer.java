package frontline.combat.fcp.client.renderer.Leopard2;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frontline.combat.fcp.client.model.Leopard2.Leopard2Model;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.client.renderer.VehicleCamoNet;
import frontline.combat.fcp.entity.vehicle.Leopard2.Leopard2Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;

public class Leopard2Renderer extends VehicleRenderer<Leopard2Entity> {

    private final VehicleCamoNet camoNet = new VehicleCamoNet();

    public Leopard2Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Leopard2Model());
    }

    @Override
    public ResourceLocation getTextureLocation(Leopard2Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    /** Also draws the camo net, after the model, from the bone frames it captured. */
    @Override
    public void render(Leopard2Entity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        camoNet.begin(entity);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        camoNet.draw(entity, bufferSource, packedLight);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Leopard2Entity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        if (!isReRender) camoNet.capture(poseStack, bone);
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
