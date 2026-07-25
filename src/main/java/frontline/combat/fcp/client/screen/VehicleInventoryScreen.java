package frontline.combat.fcp.client.screen;

import frontline.combat.fcp.entity.vehicle.VehicleInventory.InventoryStyle;
import frontline.combat.fcp.menu.VehicleInventoryMenu;
import frontline.combat.fcp.menu.VehicleInventoryMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * One screen for every vehicle hold — any number of rows, either style.
 *
 * GRID: the texture is three horizontal slices — a top cap, ONE reusable slot row, and a
 * bottom section (player inventory + edge). The row strip is blitted once per visible row,
 * so a 1-row glovebox and a 7-row hold come from the same sheet and any future size needs
 * no new art. It's also why vanilla's 6-row ceiling doesn't apply. Taller holds get a
 * scrollbar and the wider panel variant to make room for the track.
 *
 * BULK: a fixed panel showing the stored item once, with a running count — a silo rather
 * than a wall of identical stacks. Clicking the icon deposits what you're holding or pulls
 * a stack back out.
 */
public class VehicleInventoryScreen extends AbstractContainerScreen<VehicleInventoryMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("fcp", "textures/gui/vehicle_inventory.png");
    private static final int TEX_W = 512, TEX_H = 512;

    private static final int TOP_H = 17;
    private static final int ROW_H = 18;
    private static final int BOTTOM_H = 97;

    private static final int NARROW_U = 0,   NARROW_W = 176;
    private static final int WIDE_U   = 200, WIDE_W   = 194;
    private static final int KNOB_U = 400, KNOB_V = 0, KNOB_W = 12, KNOB_H = 15;
    private static final int BULK_U = 0, BULK_V = 140;
    /** One bulk channel's band; tiled once per channel. */
    private static final int CHANNEL_H = 36;

    private static final int TRACK_X = 175, TRACK_Y = 18;

    /** The item well, blitted per channel (no longer baked into the band). */
    private static final int WELL_U = 460, WELL_V = 0, FRAME_S = 26;
    /** Side margin for the channel grid. */
    private static final int GRID_MARGIN = 8;
    /** A cell at least this wide has room for "count / capacity" rather than just the count. */
    private static final int WIDE_CELL = 70;

    private final boolean bulk;
    private final boolean scrollable;
    private final int panelU;
    private boolean draggingKnob;

    public VehicleInventoryScreen(VehicleInventoryMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.bulk = menu.getStyle() == InventoryStyle.BULK;
        this.scrollable = menu.isScrollable();
        this.panelU = this.scrollable ? WIDE_U : NARROW_U;
        this.imageWidth = this.scrollable ? WIDE_W : NARROW_W;
        this.imageHeight = TOP_H
                + (this.bulk ? bulkRows() * CHANNEL_H : menu.getVisibleRows() * ROW_H)
                + BOTTOM_H;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private int trackTravel() {
        return this.menu.getVisibleRows() * ROW_H - 2 - KNOB_H;
    }

    /** Sort icon inset in the title bar, right-aligned. Clears the scroll track when present. */
    private static final int SORT_SIZE = 14;

    @Override
    protected void init() {
        super.init();
        // A Sort button only makes sense for a real grid of stacks — BULK is one item per
        // channel, and NONE has no cargo.
        boolean grid = !this.bulk && this.menu.getVisibleRows() > 0;
        if (grid) {
            // Tuck it against the panel's inner right edge, level with the title. On a
            // scrollable panel the scroll track owns the far-right strip, so sit the button
            // just left of it there instead of overlapping.
            int rightEdge = this.scrollable ? TRACK_X - 2 : this.imageWidth - 6;
            int bx = this.leftPos + rightEdge - SORT_SIZE;
            int by = this.topPos + 2;
            this.addRenderableWidget(new SortButton(bx, by, this::sendSort));
        }
    }

    private void sendSort() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) {
            mc.gameMode.handleInventoryButtonClick(this.menu.containerId, VehicleInventoryMenu.BUTTON_SORT);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        if (this.bulk) {
            // Same three-slice trick as the grid: the channel band tiles, so 1, 2 or 5
            // channels all come from one strip of art.
            int bulkCursor = y;
            graphics.blit(TEXTURE, x, bulkCursor, BULK_U, BULK_V, this.imageWidth, TOP_H, TEX_W, TEX_H);
            bulkCursor += TOP_H;
            // One band per ROW of channels, not per channel — the band is plain now, and the
            // wells are blitted per cell below so any column count works from one strip.
            for (int row = 0; row < bulkRows(); row++) {
                graphics.blit(TEXTURE, x, bulkCursor, BULK_U, BULK_V + TOP_H,
                        this.imageWidth, CHANNEL_H, TEX_W, TEX_H);
                bulkCursor += CHANNEL_H;
            }
            graphics.blit(TEXTURE, x, bulkCursor, BULK_U, BULK_V + TOP_H + CHANNEL_H,
                    this.imageWidth, BOTTOM_H, TEX_W, TEX_H);

            // Wells sit on the background so items and text draw over them.
            for (int channel = 0; channel < this.menu.getChannels(); channel++) {
                graphics.blit(TEXTURE, wellX(channel), wellY(channel),
                        WELL_U, WELL_V, FRAME_S, FRAME_S, TEX_W, TEX_H);
            }
            return;
        }

        int cursor = y;
        graphics.blit(TEXTURE, x, cursor, panelU, 0, this.imageWidth, TOP_H, TEX_W, TEX_H);
        cursor += TOP_H;
        for (int row = 0; row < this.menu.getVisibleRows(); row++) {
            graphics.blit(TEXTURE, x, cursor, panelU, TOP_H, this.imageWidth, ROW_H, TEX_W, TEX_H);
            cursor += ROW_H;
        }
        graphics.blit(TEXTURE, x, cursor, panelU, TOP_H + ROW_H, this.imageWidth, BOTTOM_H, TEX_W, TEX_H);

        if (this.scrollable) {
            int max = this.menu.maxScrollRow();
            float progress = max == 0 ? 0f : (float) this.menu.getScrollRow() / max;
            int knobY = y + TRACK_Y + Math.round(progress * trackTravel());
            graphics.blit(TEXTURE, x + TRACK_X, knobY, KNOB_U, KNOB_V, KNOB_W, KNOB_H, TEX_W, TEX_H);
        }
    }

    // ── Channel grid layout ─────────────────────────────────────────────────────
    // One or two channels stack as full-width rows (well on the left, name and count beside
    // it). Three or more spread horizontally into a grid — 6 channels becomes 3 wide by 2
    // tall — where each cell is just the well with its count underneath, since there isn't
    // room for the name. Hovering still shows the full details.

    // Both come from the menu: it sizes the panel and places the player-inventory slots
    // against the same numbers, so keeping one copy avoids the two drifting apart.
    private int bulkCols() {
        return this.menu.bulkCols();
    }

    private int bulkRows() {
        return this.menu.bulkRows();
    }

    private int cellWidth() {
        return (this.imageWidth - 2 * GRID_MARGIN) / bulkCols();
    }

    /** Left edge of a channel's well, absolute. */
    private int wellX(int channel) {
        int col = channel % bulkCols();
        int cellX = this.leftPos + GRID_MARGIN + col * cellWidth();
        return cellX + (cellWidth() - FRAME_S) / 2; // centred in its cell
    }

    /** Top edge of a channel's well, absolute. */
    private int wellY(int channel) {
        int row = channel / bulkCols();
        // The well sits high in its band so the count fits underneath: 1 + 26 + 1 + 8 = 36.
        return this.topPos + TOP_H + row * CHANNEL_H + 1;
    }

    /** Each channel's item and count, centred in its cell. */
    private void renderBulk(GuiGraphics graphics) {
        boolean roomForCapacity = cellWidth() >= WIDE_CELL;

        for (int channel = 0; channel < this.menu.getChannels(); channel++) {
            int wx = wellX(channel);
            int wy = wellY(channel);
            ItemStack type = this.menu.bulkTypeOf(channel);

            Component label;
            int colour;
            if (type.isEmpty()) {
                label = Component.translatable("gui.fcp.bulk.empty");
                colour = 0x808080;
            } else {
                // Draw the item once, without vanilla's stack-count overlay — the real total
                // dwarfs a stack and is printed underneath instead.
                graphics.renderItem(type, wx + 5, wy + 5);
                String count = formatCount(this.menu.bulkCountOf(channel));
                // Two columns leave room for the capacity too; three don't, so the tooltip
                // carries it there.
                label = Component.literal(roomForCapacity
                        ? count + " / " + formatCount(this.menu.bulkCapacityOf(channel))
                        : count);
                colour = 0x404040;
            }

            // Centre the label on the cell, not the well, so long text uses the full width.
            int cellX = this.leftPos + GRID_MARGIN + (channel % bulkCols()) * cellWidth();
            int tx = cellX + cellWidth() / 2 - this.font.width(label) / 2;
            graphics.drawString(this.font, label, tx, wy + FRAME_S + 1, colour, false);
        }
    }

    private static String formatCount(int count) {
        return String.format("%,d", count);
    }

    /** Which channel's well the mouse is over, or -1. */
    private int frameUnderMouse(double mouseX, double mouseY) {
        if (!this.bulk) return -1;
        for (int channel = 0; channel < this.menu.getChannels(); channel++) {
            int wx = wellX(channel);
            int wy = wellY(channel);
            if (mouseX >= wx && mouseX < wx + FRAME_S && mouseY >= wy && mouseY < wy + FRAME_S) {
                return channel;
            }
        }
        return -1;
    }

    /**
     * Bulk deposit/withdraw and scrolling both change server state, and both ride vanilla's
     * menu-button packet — so neither needs an FCP network channel.
     */
    private void sendButton(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) {
            mc.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    private void scrollTo(int row) {
        int clamped = Mth.clamp(row, 0, this.menu.maxScrollRow());
        if (clamped == this.menu.getScrollRow()) return;
        this.menu.setScrollRow(clamped); // move the knob now; contents follow
        sendButton(clamped);
    }

    private void scrollToMouse(double mouseY) {
        int travel = trackTravel();
        if (travel <= 0) return;
        float progress = Mth.clamp((float) (mouseY - (this.topPos + TRACK_Y) - KNOB_H / 2f) / travel, 0f, 1f);
        scrollTo(Math.round(progress * this.menu.maxScrollRow()));
    }

    private boolean overTrack(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + TRACK_X && mouseX < this.leftPos + TRACK_X + KNOB_W
                && mouseY >= this.topPos + TRACK_Y
                && mouseY < this.topPos + TRACK_Y + this.menu.getVisibleRows() * ROW_H;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.scrollable) {
            // Wheel up (positive) moves toward the top of the hold.
            scrollTo(this.menu.getScrollRow() - (int) Math.signum(delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int frame = frameUnderMouse(mouseX, mouseY);
        if (frame >= 0) {
            if (hasShiftDown()) {
                // Shift-click behaves like shift-clicking any slot: a stack goes straight
                // to the player's inventory, no hand involved.
                sendButton(VehicleInventoryMenu.BUTTON_BULK_QUICK_MOVE_BASE + frame);
            } else {
                // Holding something -> put it in (the server routes it to the right
                // channel); empty handed -> take a stack out of THIS channel.
                sendButton(this.menu.getCarried().isEmpty()
                        ? VehicleInventoryMenu.BUTTON_BULK_WITHDRAW_BASE + frame
                        : VehicleInventoryMenu.BUTTON_BULK_DEPOSIT);
            }
            return true;
        }
        if (this.scrollable && overTrack(mouseX, mouseY)) {
            this.draggingKnob = true;
            scrollToMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingKnob) {
            scrollToMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingKnob = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.bulk) renderBulk(graphics);
        this.renderTooltip(graphics, mouseX, mouseY);

        int frame = frameUnderMouse(mouseX, mouseY);
        if (frame >= 0) {
            ItemStack type = this.menu.bulkTypeOf(frame);
            if (!type.isEmpty()) {
                graphics.renderTooltip(this.font, type, mouseX, mouseY);
            }
        }
    }
}