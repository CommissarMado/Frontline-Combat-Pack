package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.HueyDoorGunnerM134Model;
import frontline.combat.fcp.client.model.Huey.HueyDoorGunnerM60Model;
import frontline.combat.fcp.entity.vehicle.Huey.HueyDoorGunnerM134Entity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyDoorGunnerM60Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HueyDoorGunnerM134Renderer extends VehicleRenderer<HueyDoorGunnerM134Entity> {
    public HueyDoorGunnerM134Renderer(EntityRendererProvider.Context renderManager) {super(renderManager, new HueyDoorGunnerM134Model());}

    @Override
    public ResourceLocation getTextureLocation(HueyDoorGunnerM134Entity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
