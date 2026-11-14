package frontline.combat.fcp.client.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

/**
 * A HUD layer written against the old Forge overlay signature (gui, graphics, partial tick, width,
 * height). NeoForge draws HUD layers as {@link LayeredDraw.Layer}; this adapts one to the other so
 * FCP's overlays keep their render methods.
 */
@FunctionalInterface
public interface IGuiOverlay extends LayeredDraw.Layer {

    void render(Gui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight);

    @Override
    default void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        render(Minecraft.getInstance().gui, guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true),
                guiGraphics.guiWidth(), guiGraphics.guiHeight());
    }
}
