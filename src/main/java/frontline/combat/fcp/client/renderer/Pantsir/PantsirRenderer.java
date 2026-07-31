package frontline.combat.fcp.client.renderer.Pantsir;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Pantsir.PantsirModel;
import frontline.combat.fcp.entity.vehicle.Pantsir.PantsirEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PantsirRenderer extends VehicleRenderer<PantsirEntity> {
    public PantsirRenderer(EntityRendererProvider.Context ctx) {super(ctx, new PantsirModel());}
    @Override public ResourceLocation getTextureLocation(PantsirEntity e) {return e.getCurrentTexture();}
}
