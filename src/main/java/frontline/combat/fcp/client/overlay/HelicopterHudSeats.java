package frontline.combat.fcp.client.overlay;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Map;

/**
 * Which helicopter seats SBW's stock {@code HelicopterHud} is cancelled for. Shared by
 * {@link frontline.combat.fcp.mixins.HelicopterHudSeatVisibilityMixin} (which does the cancelling)
 * and {@link FcpPilotOverlay} (which only draws where the stock HUD doesn't, so a seat never gets
 * both and their weapon/ammo text doesn't overlap).
 */
public final class HelicopterHudSeats {

    // Per-vehicle seat list: [ vehicle, [seats that don't get HelicopterHud's flight-instrument
    // overlay] ]. Keys are the entity id path (no namespace), e.g. "uh60_minigun" matches
    // fcp:uh60_minigun - same convention FcpPilotOverlay.PILOT_OVERLAY_VEHICLES uses.
    private static final Map<String, List<Integer>> NO_HELICOPTER_HUD_SEATS = Map.of(
            "uh60_minigun", List.of(2, 3),
            // Same reason as VehicleHudSeatVisibilityMixin's map: seat 1 (copilot) on these three
            // now has an inert "CameraTrack" weapon just to drive the front camera bone, not a
            // real gun, so it shouldn't suddenly gain the flight-instrument gunner HUD either.
            "venom", List.of(1),
            // Seats 2/3 are the door gunners - same treatment as the Black Hawk's own gunner
            // seats above, so they don't get the pilot's flight-instrument overlay either.
            "venom_gunship", List.of(1, 2, 3),
            "venom_door_guns", List.of(1, 2, 3),
            // Huey armed variants: seats 2/3 are the door gunners (M60/M134 left/right), same
            // treatment as the UH-60's own gunner seats. Seat 1 (copilot) carries no weapon at
            // all on these variants (no CameraTrack-style trick needed - the Huey has no front
            // sensor), so it never triggers this HUD in the first place and doesn't need an entry.
            "huey_m60_door_guns", List.of(2, 3),
            "huey_m60_gunship", List.of(2, 3),
            "huey_m134_door_guns", List.of(2, 3),
            "huey_m134_gunship", List.of(2, 3),
            // Mi-17 door-gun variant: seats 2/3 are the pintle-mounted PKM door gunners
            // (PKMRight/PKMLeft), same treatment as the UH-60/Huey/Venom gunner seats above.
            "mi17_door_guns", List.of(2, 3),
            // Mi-8 door-gun variant only has the one pintle PKM (seat 2, "PKMLeft"). Seat 1 is
            // the nose gunner (real "NoseGun" weapon, copilot position) - left OUT of this list
            // deliberately, since that seat is meant to keep the pilot-style flight HUD.
            "mi8_door_guns", List.of(2)
    );

    private HelicopterHudSeats() {}

    /** True if the stock HelicopterHud is suppressed for this seat of this vehicle. */
    public static boolean isStockHudHidden(VehicleEntity vehicle, int seatIndex) {
        List<Integer> hiddenSeats = NO_HELICOPTER_HUD_SEATS.get(EntityType.getKey(vehicle.getType()).getPath());
        return hiddenSeats != null && hiddenSeats.contains(seatIndex);
    }
}
