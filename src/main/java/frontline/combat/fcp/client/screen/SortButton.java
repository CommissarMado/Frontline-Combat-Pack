package frontline.combat.fcp.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A small icon button drawn from the vehicle-inventory sheet, sized and styled to sit inside
 * the hold panel rather than float over it like a default Button. Normal and hover sprites
 * live side by side on the sheet.
 */
public class SortButton extends AbstractButton {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("fcp", "textures/gui/vehicle_inventory.png");
    private static final int TEX_W = 512, TEX_H = 512;
    private static final int SPRITE = 14;
    private static final int U_NORMAL = 430, U_HOVER = 446, V = 0;

    private final Runnable onPress;

    public SortButton(int x, int y, Runnable onPress) {
        super(x, y, SPRITE, SPRITE, Component.translatable("gui.fcp.sort"));
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int u = this.isHoveredOrFocused() ? U_HOVER : U_NORMAL;
        graphics.blit(TEXTURE, getX(), getY(), u, V, SPRITE, SPRITE, TEX_W, TEX_H);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}