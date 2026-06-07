package frontline.combat.fcp.entity.vehicle;

import net.minecraft.resources.ResourceLocation;

public interface ICamoVehicle {

    int getCamoType();

    void setCamoType(int camoType);

    void cycleCamo();

    ResourceLocation[] getCamoTextures();

    String[] getCamoNames();
}
