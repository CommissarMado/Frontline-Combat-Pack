package frontline.combat.fcp.client.renderer.GazTigr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GazTigr.GazTigrModel;
import frontline.combat.fcp.client.model.Ural.UralModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GazTigrRenderer extends FCPVehicleRenderer<GazTigrEntity> {
    public GazTigrRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GazTigrModel());
    }

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(GazTigrEntity entity) {
        return entity.getCurrentTexture();
    }
}
