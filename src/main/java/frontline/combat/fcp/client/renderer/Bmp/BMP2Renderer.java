package frontline.combat.fcp.client.renderer.Bmp;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.BMP1Model;
import frontline.combat.fcp.client.model.Bmp.BMP2Model;
import frontline.combat.fcp.client.model.Uaz.UAZModel;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP2Entity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BMP2Renderer extends VehicleRenderer<BMP2Entity> {

    public BMP2Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BMP2Model());}

    @Override
    public ResourceLocation getTextureLocation(BMP2Entity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        } else {
            return textures[0];
        }
    }
}
