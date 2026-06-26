package frontline.combat.fcp.client.renderer.Ural;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ural.UralGradModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralGradEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class UralGradRenderer extends FCPVehicleRenderer<UralGradEntity> {
    public UralGradRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new UralGradModel());
    }

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(UralGradEntity entity) {
        return entity.getCurrentTexture();
    }
}
