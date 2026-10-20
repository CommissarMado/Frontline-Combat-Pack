package frontline.combat.fcp.client.renderer.Btr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr.BTR4E1Model;
import frontline.combat.fcp.entity.vehicle.Btr.BTR4E1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BTR4E1Renderer extends VehicleRenderer<BTR4E1Entity> {
    public BTR4E1Renderer(EntityRendererProvider.Context c) { super(c, new BTR4E1Model()); }
    @Override public ResourceLocation getTextureLocation(BTR4E1Entity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
