package frontline.combat.fcp.client.renderer.GazTigr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GazTigr.GazTigrMGModel;
import frontline.combat.fcp.client.model.GazTigr.GazTigrRWSModel;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrMGEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrRWSEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GazTigrMGRenderer extends VehicleRenderer<GazTigrMGEntity> {
    public GazTigrMGRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GazTigrMGModel());
    }

    @Override
    public ResourceLocation getTextureLocation(GazTigrMGEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
