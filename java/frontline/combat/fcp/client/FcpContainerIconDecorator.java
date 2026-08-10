package frontline.combat.fcp.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

/**
 * Draws the FCP vehicle icon over the container item slot in GUIs (creative tab, inventory).
 * Paired with {@code ContainerBlockItemRendererMixin}, which suppresses the 3D crate so the
 * icon fully replaces it. If that mixin ever fails to apply, the icon still draws on top.
 */
public class FcpContainerIconDecorator implements IItemDecorator {

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        ResourceLocation icon = FcpContainerIcon.getIcon(stack);
        if (icon == null) return false;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0, 0.0, 200.0); // draw in front of the item slot
        // The 128x128 PNG is drawn scaled into the 16x16 slot (full texture -> full slot).
        guiGraphics.blit(icon, xOffset, yOffset, 0.0f, 0.0f, 16, 16, 16, 16);
        guiGraphics.pose().popPose();
        return true;
    }
}
