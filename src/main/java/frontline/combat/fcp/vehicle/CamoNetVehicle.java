package frontline.combat.fcp.vehicle;

/**
 * A vehicle that takes a camo net (fcp:camo_net, see CamoNetHandler and VehicleCamoNet). Preset: 0 none, 1 light, 2 heavy;
 * colour: 0 jungle, 1 woodland, 2 olive, 3 timber, 4 desert. Both are synced and saved by the vehicle.
 */
public interface CamoNetVehicle {

    int getCamoNet();

    /** Next preset this vehicle has; returns the new one. */
    int cycleCamoNet();

    int getCamoNetColor();

    /** Next colour; returns the new one. */
    int cycleCamoNetColor();

    /** Net mesh name: assets/fcp/camo_net/&lt;name&gt;[_heavy].json. */
    String camoNetName();
}
