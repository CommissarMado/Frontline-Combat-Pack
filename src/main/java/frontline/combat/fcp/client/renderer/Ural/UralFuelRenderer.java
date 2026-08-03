package frontline.combat.fcp.client.renderer.Ural;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ural.UralFuelModel;
import frontline.combat.fcp.entity.vehicle.Ural.UralFuelEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class UralFuelRenderer extends VehicleRenderer<UralFuelEntity> {
    public UralFuelRenderer(EntityRendererProvider.Context ctx) {super(ctx, new UralFuelModel());}
    @Override public ResourceLocation getTextureLocation(UralFuelEntity e) {return e.getCurrentTexture();}
}
