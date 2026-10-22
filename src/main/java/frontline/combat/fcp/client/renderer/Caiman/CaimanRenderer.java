package frontline.combat.fcp.client.renderer.Caiman;

import frontline.combat.fcp.client.model.Caiman.CaimanModel;
import frontline.combat.fcp.client.renderer.Humvee.HumveeUnarmedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Humvee renderer (skins + attachment interaction boxes) with the Caiman model. */
public class CaimanRenderer extends HumveeUnarmedRenderer {

    public CaimanRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CaimanModel());
    }
}
