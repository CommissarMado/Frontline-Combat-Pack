package frontline.combat.fcp.client.renderer.Ch47;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Ch47.Ch47Model;
import frontline.combat.fcp.client.renderer.BoneMeshes;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Ch47.Ch47Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;

/** The CH-47 is a mesh model: its geo holds the bones and cubes, the meshes come from BoneMeshes. */
public class Ch47Renderer extends VehicleRenderer<Ch47Entity> {

    private static final ResourceLocation MESHES = new ResourceLocation(FCP.MODID, "meshes/ch47.json");

    public Ch47Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Ch47Model());
    }

    @Override
    public ResourceLocation getTextureLocation(Ch47Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight,
                                  int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        if (!bone.isHidden()) {
            BoneMeshes.render(BoneMeshes.get(MESHES), bone, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
