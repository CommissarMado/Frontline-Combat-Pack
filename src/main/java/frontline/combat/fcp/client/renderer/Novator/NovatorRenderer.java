package frontline.combat.fcp.client.renderer.Novator;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Novator.NovatorModel;
import frontline.combat.fcp.entity.vehicle.Novator.NovatorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class NovatorRenderer extends VehicleRenderer<NovatorEntity> {
    public NovatorRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new NovatorModel());}

    @Override
    public ResourceLocation getTextureLocation(NovatorEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        } else {
            return textures[0];
        }
    }
}
