package frontline.combat.fcp.entity.vehicle;

/**
 * A vehicle with a launcher that is raised (or opened) while its gunner has the ATGM selected and lowered
 * while it reloads; animated client-side by AtgmLauncherAnimation.
 */
public interface AtgmLauncherVehicle {

    /** False for variants of the class that don't carry the launcher. */
    boolean hasAtgmLauncher();

    /** The gunner weapon key of the ATGM. */
    String atgmWeapon();

    /** True while the ATGM is reloading (synced to clients). */
    boolean atgmReloading();
}
