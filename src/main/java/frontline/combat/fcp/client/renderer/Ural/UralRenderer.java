package frontline.combat.fcp.client.renderer.Ural;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ural.UralModel;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class UralRenderer extends VehicleRenderer<UralEntity> {
    public UralRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new UralModel());
    }

    @Override
    public ResourceLocation getTextureLocation(UralEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }

        return textures[0];
    }
}
