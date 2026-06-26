package frontline.combat.fcp.client.renderer.Ural;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ural.UralGradModel;
import frontline.combat.fcp.client.model.Ural.UralModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralGradEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class UralRenderer extends FCPVehicleRenderer<UralEntity> {
    public UralRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new UralModel());
    }

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(UralEntity entity) {
        return entity.getCurrentTexture();
    }
}
