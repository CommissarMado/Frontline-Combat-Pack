package frontline.combat.fcp.client.renderer.MemeVehicles;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Littlebird.LittlebirdModel;
import frontline.combat.fcp.client.model.MemeVehicles.BigBirdModel;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdEntity;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.BigBirdEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BigBirdRenderer extends VehicleRenderer<BigBirdEntity> {
    public BigBirdRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new BigBirdModel());}

    @Override
    public ResourceLocation getTextureLocation(BigBirdEntity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
