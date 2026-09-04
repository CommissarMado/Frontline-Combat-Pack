package frontline.combat.fcp.client.renderer.Emplacement;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Emplacement.EmplDshkModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplDshkEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class EmplDshkRenderer extends VehicleRenderer<EmplDshkEntity> {
    public EmplDshkRenderer(EntityRendererProvider.Context ctx) {super(ctx, new EmplDshkModel());}
    @Override public ResourceLocation getTextureLocation(EmplDshkEntity e) {return FcpVehicleTexture.resolve(e, e.getCurrentTexture());}
}
