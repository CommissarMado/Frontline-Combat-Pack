package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.overlay.weapon.HelicopterHud;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Suppresses SBW's {@code HelicopterHud} - the full flight-instrument aiming HUD (compass, roll
 * indicator, artificial-horizon pitch ladder, power/VSI ruler, speed/altitude readouts) drawn by
 * {@link com.atsuishio.superbwarfare.client.overlay.VehicleMainWeaponHudOverlay} whenever a
 * vehicle's JSON {@code HudType} is {@code "@Helicopter"} - for specific seats of specific FCP
 * vehicles.
 *
 * <p>This HUD is dispatched purely off the VEHICLE's {@code HudType}, not the seat: every seat
 * that has a weapon assigned falls into {@code HelicopterHud.render()}'s "else" branch and gets
 * the full pilot instrument overlay, regardless of whether that seat is actually meant to fly the
 * aircraft. For the UH-60's door-gunner seats (which have {@code MinigunLeft}/{@code MinigunRight}
 * assigned so they can aim), that means they show the exact same compass/roll/power/altitude HUD
 * as the pilot, which looks wrong for a fixed door gun. A seat with NO weapon assigned never sees
 * any of this - {@code HelicopterHud.render()}'s very first check, {@code getGunData(index) ==
 * null}, returns immediately - so cancelling the whole method for the listed seats makes them
 * behave exactly like an unarmed passenger seat, matching {@link VehicleHudSeatVisibilityMixin}'s
 * effect on the other HUD.
 *
 * <p>Mirrors {@link frontline.combat.fcp.client.overlay.FcpPilotOverlay}'s per-vehicle seat-list
 * map, so adding another vehicle here later is a one-line entry.
 */
@Mixin(value = HelicopterHud.class, remap = false)
public abstract class HelicopterHudSeatVisibilityMixin {

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
            "venom_door_guns", List.of(1, 2, 3)
    );

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void fcp$hideForGunnerSeats(VehicleEntity vehicle, Player player, ForgeGui gui,
                                         GuiGraphics guiGraphics, float partialTick,
                                         int screenWidth, int screenHeight, CallbackInfo ci) {
        if (vehicle == null || player == null) return;

        List<Integer> hiddenSeats = NO_HELICOPTER_HUD_SEATS.get(EntityType.getKey(vehicle.getType()).getPath());
        if (hiddenSeats == null) return;

        if (hiddenSeats.contains(vehicle.getSeatIndex(player))) {
            ci.cancel();
        }
    }
}
