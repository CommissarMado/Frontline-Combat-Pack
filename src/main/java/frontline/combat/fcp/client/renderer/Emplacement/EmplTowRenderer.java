package frontline.combat.fcp.client.renderer.Emplacement;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Emplacement.EmplTowModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplTowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class EmplTowRenderer extends VehicleRenderer<EmplTowEntity> {
    public EmplTowRenderer(EntityRendererProvider.Context ctx) {super(ctx, new EmplTowModel());}
    @Override public ResourceLocation getTextureLocation(EmplTowEntity e) {return e.getCurrentTexture();}
}
