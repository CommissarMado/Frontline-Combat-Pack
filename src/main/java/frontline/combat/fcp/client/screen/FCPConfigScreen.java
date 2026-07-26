package frontline.combat.fcp.client.screen;

import frontline.combat.fcp.FCPConfig;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.RequestFcpConfigStatePacket;
import frontline.combat.fcp.network.SetMulticrewPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nullable;

/**
 * FCP's config screen, reached via Mods -> FCP -> Config.
 *
 * Two modes, decided by whether a world is open:
 *  - No world (main menu): edits the local config file directly — that's what the next
 *    singleplayer world will read.
 *  - In a world: the value shown is the SERVER's, fetched on open, and toggling sends a
 *    request the server permission-checks (op level 2, or being the singleplayer/LAN
 *    host). Non-ops see the state read-only. The change applies server-wide immediately
 *    via a datapack hot-reload.
 */
public class FCPConfigScreen extends Screen {

    private final Screen parent;

    /** Server state once it arrives; null until then (and always null at the main menu). */
    @Nullable
    private Boolean serverMulticrew;
    private boolean canEdit;
    private boolean awaitingServer;

    public FCPConfigScreen(Screen parent) {
        super(Component.translatable("gui.fcp.config.title"));
        this.parent = parent;
    }

    /** Hook for the mods-list Config button. Call once during client setup. */
    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new FCPConfigScreen(parent)));
    }

    /** Called by FcpConfigStatePacket on the render thread. */
    public static void acceptServerState(boolean multicrew, boolean canEdit) {
        if (Minecraft.getInstance().screen instanceof FCPConfigScreen screen) {
            screen.serverMulticrew = multicrew;
            screen.canEdit = canEdit;
            screen.awaitingServer = false;
            screen.rebuildWidgets();
        }
    }

    private boolean inWorld() {
        return Minecraft.getInstance().getConnection() != null;
    }

    @Override
    protected void init() {
        boolean local = !inWorld();
        if (!local && this.serverMulticrew == null && !this.awaitingServer) {
            this.awaitingServer = true;
            FCPNetwork.FCP_HANDLER.sendToServer(new RequestFcpConfigStatePacket());
        }

        boolean value = local ? FCPConfig.multicrewEnabled()
                : this.serverMulticrew != null && this.serverMulticrew;
        boolean editable = local || (this.serverMulticrew != null && this.canEdit);

        CycleButton<Boolean> toggle = CycleButton.onOffBuilder(value)
                .create(this.width / 2 - 100, this.height / 2 - 22, 200, 20,
                        Component.translatable("gui.fcp.config.multicrew"),
                        (button, newValue) -> onToggle(newValue));
        toggle.active = editable;
        addRenderableWidget(toggle);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(this.width / 2 - 100, this.height / 2 + 30, 200, 20)
                .build());
    }

    private void onToggle(boolean newValue) {
        if (inWorld()) {
            // The server decides; its state packet will rebuild this screen with the truth.
            this.awaitingServer = true;
            FCPNetwork.FCP_HANDLER.sendToServer(new SetMulticrewPacket(newValue));
        } else {
            FCPConfig.setMulticrew(newValue);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFF);

        Component status = statusLine();
        if (status != null) {
            graphics.drawCenteredString(this.font, status, this.width / 2, this.height / 2 + 6, 0xA0A0A0);
        }
    }

    @Nullable
    private Component statusLine() {
        if (!inWorld()) return Component.translatable("gui.fcp.config.local_hint");
        if (this.serverMulticrew == null) return Component.translatable("gui.fcp.config.loading");
        if (!this.canEdit) return Component.translatable("gui.fcp.config.requires_op");
        return Component.translatable("gui.fcp.config.server_hint");
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }
}