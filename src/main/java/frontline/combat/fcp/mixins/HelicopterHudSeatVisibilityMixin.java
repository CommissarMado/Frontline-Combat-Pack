package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.overlay.weapon.HelicopterHud;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.client.overlay.HelicopterHudSeats;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


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

    // The per-vehicle seat list lives in HelicopterHudSeats so FcpPilotOverlay can read it too.

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void fcp$hideForGunnerSeats(VehicleEntity vehicle, Player player, ForgeGui gui,
                                         GuiGraphics guiGraphics, float partialTick,
                                         int screenWidth, int screenHeight, CallbackInfo ci) {
        if (vehicle == null || player == null) return;

        if (HelicopterHudSeats.isStockHudHidden(vehicle, vehicle.getSeatIndex(player))) {
            ci.cancel();
        }
    }
}
