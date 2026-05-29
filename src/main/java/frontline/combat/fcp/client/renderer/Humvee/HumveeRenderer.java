package frontline.combat.fcp.client.renderer.Humvee;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Humvee.HumveeModel;
import frontline.combat.fcp.client.model.Matv.MATVModel;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HumveeRenderer extends VehicleRenderer<HumveeEntity> {

    public HumveeRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new HumveeModel());}

    @Override
    public ResourceLocation getTextureLocation(HumveeEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        } else {
            return textures[0];
        }
    }
}
