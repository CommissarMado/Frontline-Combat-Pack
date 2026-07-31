package frontline.combat.fcp.client.renderer.Kamaz;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Kamaz.KamazLongModel;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazLongEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class KamazLongRenderer extends VehicleRenderer<KamazLongEntity> {
    public KamazLongRenderer(EntityRendererProvider.Context ctx) {super(ctx, new KamazLongModel());}
    @Override public ResourceLocation getTextureLocation(KamazLongEntity e) {return e.getCurrentTexture();}
}
