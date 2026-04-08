package frontline.combat.fcp.client.renderer.T80bvm;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.BMP1Model;
import frontline.combat.fcp.client.model.T72av.T72AVModel;
import frontline.combat.fcp.client.model.T80bvm.T80BVMModel;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.T72av.T72AVEntity;
import frontline.combat.fcp.entity.vehicle.T80bvm.T80BVMEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class T80BVMRenderer extends VehicleRenderer<T80BVMEntity> {

    public T80BVMRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new T80BVMModel());}

    @Override
    public ResourceLocation getTextureLocation(T80BVMEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();

        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        } else {
            return textures[0];
        }
    }
}
