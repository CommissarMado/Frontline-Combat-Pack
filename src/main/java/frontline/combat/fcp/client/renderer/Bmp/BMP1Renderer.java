package frontline.combat.fcp.client.renderer.Bmp;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.BMP1Model;
import frontline.combat.fcp.client.model.Uaz.UAZModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BMP1Renderer extends FCPVehicleRenderer<BMP1Entity> {

    public BMP1Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BMP1Model());}

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(BMP1Entity entity) {
        return entity.getCurrentTexture();
    }
}
