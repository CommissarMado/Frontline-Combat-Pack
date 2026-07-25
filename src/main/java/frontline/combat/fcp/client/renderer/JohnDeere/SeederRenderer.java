package frontline.combat.fcp.client.renderer.JohnDeere;

import frontline.combat.fcp.client.model.JohnDeere.SeederModel;
import frontline.combat.fcp.client.renderer.Trailers.TrailerRenderer;
import frontline.combat.fcp.entity.vehicle.JohnDeere.SeederEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SeederRenderer extends TrailerRenderer<SeederEntity> {

    public SeederRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SeederModel());
    }
}