package frontline.combat.fcp.client.renderer.Emplacement;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Emplacement.EmplKornetModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplKornetEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class EmplKornetRenderer extends VehicleRenderer<EmplKornetEntity> {
    public EmplKornetRenderer(EntityRendererProvider.Context ctx) {super(ctx, new EmplKornetModel());}
    @Override public ResourceLocation getTextureLocation(EmplKornetEntity e) {return FcpVehicleTexture.resolve(e, e.getCurrentTexture());}
}
