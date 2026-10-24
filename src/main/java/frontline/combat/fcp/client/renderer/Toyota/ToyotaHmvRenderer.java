package frontline.combat.fcp.client.renderer.Toyota;

import frontline.combat.fcp.client.model.Toyota.ToyotaHmvModel;
import frontline.combat.fcp.client.renderer.Humvee.HumveeUnarmedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Humvee renderer (skins) with the Toyota HMV model. */
public class ToyotaHmvRenderer extends HumveeUnarmedRenderer {

    public ToyotaHmvRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ToyotaHmvModel());
    }
}
