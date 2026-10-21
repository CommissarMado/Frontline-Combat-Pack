package frontline.combat.fcp.client.model.Stugna;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Stugna.StugnaControllerEntity;
import net.minecraft.resources.ResourceLocation;

public class StugnaControllerModel extends FCPVehicleModel<StugnaControllerEntity> {
    @Override public ResourceLocation getModelResource(StugnaControllerEntity a) {return new ResourceLocation(FCP.MODID, "geo/stugna_pc.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
}
