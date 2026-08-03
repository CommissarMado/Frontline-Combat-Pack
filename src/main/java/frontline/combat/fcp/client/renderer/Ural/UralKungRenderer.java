package frontline.combat.fcp.client.renderer.Ural;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ural.UralKungModel;
import frontline.combat.fcp.entity.vehicle.Ural.UralKungEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class UralKungRenderer extends VehicleRenderer<UralKungEntity> {
    public UralKungRenderer(EntityRendererProvider.Context ctx) {super(ctx, new UralKungModel());}
    @Override public ResourceLocation getTextureLocation(UralKungEntity e) {return e.getCurrentTexture();}
}
