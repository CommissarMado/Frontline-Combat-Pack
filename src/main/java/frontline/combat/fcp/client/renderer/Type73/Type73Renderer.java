package frontline.combat.fcp.client.renderer.Type73;

import frontline.combat.fcp.client.model.Type73.Type73Model;
import frontline.combat.fcp.client.renderer.Humvee.HumveeUnarmedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Humvee renderer (skins + attachment interaction boxes) with the Type 73 model. */
public class Type73Renderer extends HumveeUnarmedRenderer {

    public Type73Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Type73Model());
    }
}
