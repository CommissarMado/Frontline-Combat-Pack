package frontline.combat.fcp.client.renderer.Ural;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ural.UralGradModel;
import frontline.combat.fcp.entity.vehicle.Ural.UralGradEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class UralGradRenderer extends VehicleRenderer<UralGradEntity> {
    public UralGradRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new UralGradModel());
    }

    @Override
    public ResourceLocation getTextureLocation(UralGradEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
