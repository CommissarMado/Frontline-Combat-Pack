package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.overlay.VehicleHudOverlay;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Makes specific seats of specific FCP vehicles render exactly like an unarmed passenger seat in
 * {@link VehicleHudOverlay}, instead of the seat's own weapon/ammo panel.
 *
 * <p>VehicleHudOverlay's flight-instrument panels ({@code renderGearInfo}/{@code renderHoverInfo}/
 * {@code renderSpeedInfo}) already only draw for {@code vehicle.firstPassenger} (the pilot seat),
 * and {@code renderPassengerInfo}/the health/armor/energy bars are vehicle-wide and shown to every
 * seat regardless of weapon. The ONE panel that differs between an unarmed passenger seat and a
 * gunner seat is {@code renderWeaponInfo}: it renders whenever {@code vehicle.banHand(player)} is
 * true, which - per {@code VehicleEntity.banHand()} - is true whenever the seat has a
 * {@code GunData} (i.e. a weapon assigned), regardless of the seat's {@code BanHand} JSON flag.
 * That's exactly why the UH-60's door-gunner seats (which have {@code MinigunLeft}/
 * {@code MinigunRight} assigned) show the weapon/ammo frame while a plain passenger seat doesn't.
 *
 * <p>So to make a seat look like an unarmed passenger seat, only {@code renderWeaponInfo} needs to
 * be skipped for it - everything else already behaves that way automatically. This injects at the
 * head of that method (the same method {@link VehicleHudMagazineMixin} already targets in this
 * class, via {@code @Redirect}, so targeting it here too is a proven-working pattern) and cancels
 * it outright for the listed seats.
 *
 * <p>Mirrors {@link frontline.combat.fcp.client.overlay.FcpPilotOverlay}'s per-vehicle seat-list
 * map, so adding another vehicle here later is a one-line entry.
 */
@Mixin(value = VehicleHudOverlay.class, remap = false)
public abstract class VehicleHudSeatVisibilityMixin {

    // Per-vehicle seat list: [ vehicle, [seats whose weapon/ammo panel is suppressed, so they
    // look like an unarmed passenger seat instead] ]. Keys are the entity id path (no namespace),
    // e.g. "uh60_minigun" matches fcp:uh60_minigun - same convention
    // FcpPilotOverlay.PILOT_OVERLAY_VEHICLES uses.
    private static final Map<String, List<Integer>> NO_WEAPON_PANEL_SEATS = Map.of(
            "uh60_minigun", List.of(2, 3),
            // Seat 1 on these three is the copilot - it now carries an inert "CameraTrack"
            // weapon purely so the front camera bone can read the copilot's aim (see
            // VenomEntity/VenomGunshipEntity/VenomDoorGunsEntity.getCameraYawDeg), not a real
            // gun, so it should keep looking like a normal unarmed copilot seat.
            "venom", List.of(1),
            // Seats 2/3 are the door gunners (MinigunRight/MinigunLeft) - same treatment as the
            // Black Hawk's own gunner seats above, for the same reason: they have a real weapon,
            // but they're a fixed door gun, not the pilot, so they shouldn't show the vehicle's
            // main weapon/ammo panel either.
            "venom_gunship", List.of(1, 2, 3),
            "venom_door_guns", List.of(1, 2, 3),
            // Huey armed variants: seats 2/3 are the door gunners (M60/M134 left/right), same
            // treatment as the UH-60's own gunner seats. Seat 1 (copilot) carries no weapon on
            // these variants, so it never shows this panel in the first place.
            "huey_m60_door_guns", List.of(2, 3),
            "huey_m60_gunship", List.of(2, 3),
            "huey_m134_door_guns", List.of(2, 3),
            "huey_m134_gunship", List.of(2, 3)
    );

    @Inject(method = "renderWeaponInfo", at = @At("HEAD"), cancellable = true)
    private void fcp$hideWeaponPanelForGunnerSeats(GuiGraphics guiGraphics, VehicleEntity vehicle,
                                                    int w, int h, CallbackInfo ci) {
        if (vehicle == null) return;

        List<Integer> hiddenSeats = NO_WEAPON_PANEL_SEATS.get(EntityType.getKey(vehicle.getType()).getPath());
        if (hiddenSeats == null) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (hiddenSeats.contains(vehicle.getSeatIndex(player))) {
            ci.cancel();
        }
    }
}
