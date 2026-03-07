package frontline.combat.fcp.client.renderer.T72av;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.BMP1Model;
import frontline.combat.fcp.client.model.T72av.T72AVModel;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.T72av.T72AVEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class T72AVRenderer extends VehicleRenderer<T72AVEntity> {

    public T72AVRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new T72AVModel());}

    @Override
    public ResourceLocation getTextureLocation(T72AVEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        } else {
            return textures[0];
        }
    }
}
