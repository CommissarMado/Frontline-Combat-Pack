package frontline.combat.fcp.vehicle.humvee;

/** Anything that carries the Humvee attachment state (implemented by the entity). */
public interface HumveeVehicle {
    /** Selected variant index for a category (0 if unset). */
    int getAttachmentIndex(String category);

    /** Advance a category to its next variant, wrapping. Server-authoritative. */
    void cycleAttachment(String category, int variantCount);

    /** Registry path of this vehicle (e.g. "hmmwv_cargo"), used to look up attachment data. */
    String humveeName();
}
