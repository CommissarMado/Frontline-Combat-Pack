package frontline.combat.fcp.client.renderer.GazTigr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GazTigr.GazTigrGLModel;
import frontline.combat.fcp.client.model.GazTigr.GazTigrMGModel;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrGLEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrMGEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GazTigrGLRenderer extends VehicleRenderer<GazTigrGLEntity> {
    public GazTigrGLRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GazTigrGLModel());
    }

    @Override
    public ResourceLocation getTextureLocation(GazTigrGLEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
