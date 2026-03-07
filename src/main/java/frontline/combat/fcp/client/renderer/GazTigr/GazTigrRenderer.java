package frontline.combat.fcp.client.renderer.GazTigr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GazTigr.GazTigrModel;
import frontline.combat.fcp.client.model.Ural.UralModel;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GazTigrRenderer extends VehicleRenderer<GazTigrEntity> {
    public GazTigrRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GazTigrModel());
    }

    @Override
    public ResourceLocation getTextureLocation(GazTigrEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
