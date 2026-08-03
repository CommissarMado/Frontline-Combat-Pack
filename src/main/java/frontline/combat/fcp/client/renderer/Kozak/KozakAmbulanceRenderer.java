package frontline.combat.fcp.client.renderer.Kozak;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Kozak.KozakAmbulanceModel;
import frontline.combat.fcp.entity.vehicle.Kozak.KozakAmbulanceEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class KozakAmbulanceRenderer extends VehicleRenderer<KozakAmbulanceEntity> {
    public KozakAmbulanceRenderer(EntityRendererProvider.Context ctx) {super(ctx, new KozakAmbulanceModel());}
    @Override public ResourceLocation getTextureLocation(KozakAmbulanceEntity entity) {return entity.getCurrentTexture();}
}
