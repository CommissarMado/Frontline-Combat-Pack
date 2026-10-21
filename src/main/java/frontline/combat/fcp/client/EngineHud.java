package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.engine.EngineControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;

/** Layout of the engine element in SBW's vehicle HUD (drawn by VehicleHudEngineBarMixin). */
public final class EngineHud {

    /** Left edge: just right of the health bar, which ends at x=80. */
    public static final int X = 84;
    public static final int BAR_WIDTH = 30;

    private EngineHud() {
    }

    public static String keyLabel() {
        return FCPKeybinds.ENGINE_TOGGLE.getTranslatedKeyMessage().getString();
    }

    /** Width of key label + gap + bar. */
    public static int width(Font font) {
        return Math.max(8, font.width(keyLabel())) + 2 + BAR_WIDTH;
    }

    /** True when the local player is driving a vehicle with engine start/stop (the element is shown). */
    public static boolean shown(Player player) {
        return player != null && player.getVehicle() instanceof VehicleEntity vehicle
                && EngineControl.appliesTo(vehicle) && vehicle.getFirstPassenger() == player;
    }

    /** x for SBW's hover-mode text on helicopters, moved to the right of the engine element. */
    public static int hoverTextX(int sbwX) {
        Minecraft mc = Minecraft.getInstance();
        if (!shown(mc.player)) return sbwX;
        return Math.max(sbwX, X + width(mc.font) + 4);
    }
}
