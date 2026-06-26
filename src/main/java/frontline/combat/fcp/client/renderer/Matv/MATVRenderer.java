package frontline.combat.fcp.client.renderer.Matv;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Matv.MATVModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.Matv.MATVEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MATVRenderer extends FCPVehicleRenderer<MATVEntity> {

    public MATVRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new MATVModel());}

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(MATVEntity entity) {
        return entity.getCurrentTexture();
    }
}