package frontline.combat.fcp.client.renderer.Stugna;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Stugna.StugnaControllerModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Stugna.StugnaControllerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class StugnaControllerRenderer extends VehicleRenderer<StugnaControllerEntity> {
    public StugnaControllerRenderer(EntityRendererProvider.Context ctx) {super(ctx, new StugnaControllerModel());}
    @Override public ResourceLocation getTextureLocation(StugnaControllerEntity e) {return FcpVehicleTexture.resolve(e, e.getCurrentTexture());}
}
