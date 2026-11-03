package frontline.combat.fcp.client.renderer.Komatsu;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Komatsu.KomatsuLavModel;
import frontline.combat.fcp.client.renderer.BoneMeshes;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Komatsu.KomatsuLavEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.cache.object.GeoBone;

/** The Komatsu LAVs are mesh models (the shield's gun shield): geo cubes plus BoneMeshes per registry name. */
public class KomatsuLavRenderer extends VehicleRenderer<KomatsuLavEntity> {

    public KomatsuLavRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KomatsuLavModel());
    }

    @Override
    public ResourceLocation getTextureLocation(KomatsuLavEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight,
                                  int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        KomatsuLavEntity current = getAnimatable();
        if (!bone.isHidden() && current != null) {
            ResourceLocation meshes = new ResourceLocation(FCP.MODID, "meshes/" + EntityType.getKey(current.getType()).getPath() + ".json");
            BoneMeshes.render(BoneMeshes.get(meshes), bone, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
