package frontline.combat.fcp.client.renderer.Dingo;

import frontline.combat.fcp.client.model.Dingo.DingoModel;
import frontline.combat.fcp.client.renderer.Humvee.HumveeUnarmedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Humvee renderer (skins + attachment interaction boxes) with the Dingo model. */
public class DingoRenderer extends HumveeUnarmedRenderer {

    public DingoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DingoModel());
    }
}
