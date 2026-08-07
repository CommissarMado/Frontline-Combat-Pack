package frontline.combat.fcp.client.renderer.Dpv;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Dpv.DpvMinigunModel;
import frontline.combat.fcp.entity.vehicle.Dpv.DpvMinigunEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DpvMinigunRenderer extends VehicleRenderer<DpvMinigunEntity> {
    public DpvMinigunRenderer(EntityRendererProvider.Context ctx) {super(ctx, new DpvMinigunModel());}
    @Override public ResourceLocation getTextureLocation(DpvMinigunEntity entity) {return entity.getCurrentTexture();}
}
