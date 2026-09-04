package frontline.combat.fcp.client.renderer.Kamaz;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Kamaz.KamazKungModel;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazKungEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class KamazKungRenderer extends VehicleRenderer<KamazKungEntity> {
    public KamazKungRenderer(EntityRendererProvider.Context ctx) {super(ctx, new KamazKungModel());}
    @Override public ResourceLocation getTextureLocation(KamazKungEntity e) {return FcpVehicleTexture.resolve(e, e.getCurrentTexture());}
}
