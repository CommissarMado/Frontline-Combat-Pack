package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.HueyModel;
import frontline.combat.fcp.client.model.Huey.HueyRocketsModel;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyRocketsEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HueyRocketsRenderer extends VehicleRenderer<HueyRocketsEntity> {
    public HueyRocketsRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new HueyRocketsModel());}

    @Override
    public ResourceLocation getTextureLocation(HueyRocketsEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
