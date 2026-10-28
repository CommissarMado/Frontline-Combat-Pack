package frontline.combat.fcp.entity.vehicle;

/**
 * A vehicle with something over its roof (e.g. the T-90M's cope cage) that scales every hit coming in from above,
 * with or without FCP's armour model (VehicleArmor applies it to SBW's damage too).
 */
public interface RoofCover {

    /** Multiplier for damage arriving from above; 1 = no cover. */
    float roofDamageMultiplier();
}
