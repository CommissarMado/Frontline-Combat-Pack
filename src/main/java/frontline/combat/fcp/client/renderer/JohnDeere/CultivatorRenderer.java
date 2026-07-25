package frontline.combat.fcp.client.renderer.JohnDeere;

import frontline.combat.fcp.client.model.JohnDeere.CultivatorModel;
import frontline.combat.fcp.client.renderer.Trailers.TrailerRenderer;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CultivatorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CultivatorRenderer extends TrailerRenderer<CultivatorEntity> {

    public CultivatorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CultivatorModel());
    }
}