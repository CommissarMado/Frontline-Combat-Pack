package frontline.combat.fcp.client.renderer.Stugna;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Stugna.StugnaLauncherModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Stugna.StugnaLauncherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class StugnaLauncherRenderer extends VehicleRenderer<StugnaLauncherEntity> {
    public StugnaLauncherRenderer(EntityRendererProvider.Context ctx) {super(ctx, new StugnaLauncherModel());}
    @Override public ResourceLocation getTextureLocation(StugnaLauncherEntity e) {return FcpVehicleTexture.resolve(e, e.getCurrentTexture());}
}
