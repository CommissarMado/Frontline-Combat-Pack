package frontline.combat.fcp.client.renderer.Bmp;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.BMP1Model;
import frontline.combat.fcp.client.model.Bmp.BMP1UModel;
import frontline.combat.fcp.client.model.Uaz.UAZModel;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1UEntity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BMP1URenderer extends VehicleRenderer<BMP1UEntity> {

    public BMP1URenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BMP1UModel());}

    @Override
    public ResourceLocation getTextureLocation(BMP1UEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        } else {
            return textures[0];
        }
    }
}
