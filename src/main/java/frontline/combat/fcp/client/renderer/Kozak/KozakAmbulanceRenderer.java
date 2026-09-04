package frontline.combat.fcp.client.renderer.Kozak;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Kozak.KozakAmbulanceModel;
import frontline.combat.fcp.entity.vehicle.Kozak.KozakAmbulanceEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class KozakAmbulanceRenderer extends VehicleRenderer<KozakAmbulanceEntity> {
    public KozakAmbulanceRenderer(EntityRendererProvider.Context ctx) {super(ctx, new KozakAmbulanceModel());}
    @Override public ResourceLocation getTextureLocation(KozakAmbulanceEntity entity) {return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());}
}
