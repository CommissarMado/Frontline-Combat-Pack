package frontline.combat.fcp.client.renderer.GazTigr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GazTigr.GazTigrModel;
import frontline.combat.fcp.client.model.GazTigr.GazTigrRWSModel;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrRWSEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GazTigrRWSRenderer extends VehicleRenderer<GazTigrRWSEntity> {
    public GazTigrRWSRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GazTigrRWSModel());
    }

    @Override
    public ResourceLocation getTextureLocation(GazTigrRWSEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
