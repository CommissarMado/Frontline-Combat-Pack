package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.HueyDoorGunnerM60Model;
import frontline.combat.fcp.client.model.Huey.HueyModel;
import frontline.combat.fcp.entity.vehicle.Huey.HueyDoorGunnerM60Entity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HueyDoorGunnerM60Renderer extends VehicleRenderer<HueyDoorGunnerM60Entity> {
    public HueyDoorGunnerM60Renderer(EntityRendererProvider.Context renderManager) {super(renderManager, new HueyDoorGunnerM60Model());}

    @Override
    public ResourceLocation getTextureLocation(HueyDoorGunnerM60Entity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
