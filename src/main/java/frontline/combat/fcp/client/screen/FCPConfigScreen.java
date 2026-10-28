package frontline.combat.fcp.client.screen;

import frontline.combat.fcp.FCPConfig;
import frontline.combat.fcp.FcpWorldSettings;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.RequestFcpConfigStatePacket;
import frontline.combat.fcp.network.SetMulticrewPacket;
import frontline.combat.fcp.network.SetWorldSettingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nullable;
import java.util.List;

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
    /** Artillery vehicle shown in the per-vehicle row; static so it survives widget rebuilds. */
    private static int selectedArtillery;

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
        // World settings only exist while a world is open.
        boolean worldEditable = !local && this.serverMulticrew != null && this.canEdit;
        FcpWorldSettings.Snapshot world = FcpWorldSettings.current();
        int cx = this.width / 2, y = top();

        CycleButton<Boolean> toggle = CycleButton.onOffBuilder(value)
                .create(cx - 100, y, 200, 20,
                        Component.translatable("gui.fcp.config.multicrew"),
                        (button, newValue) -> onToggle(newValue));
        toggle.active = editable;
        addRenderableWidget(toggle);

        CycleButton<Boolean> lock = CycleButton.onOffBuilder(world.lockInventories())
                .create(cx - 100, y + 24, 200, 20,
                        Component.translatable("gui.fcp.config.lock_inventories"),
                        (button, newValue) -> sendWorld(SetWorldSettingPacket.Kind.LOCK_INVENTORIES, "", newValue ? 1 : 0));
        lock.active = worldEditable;
        addRenderableWidget(lock);

        CycleButton<Boolean> fill = CycleButton.onOffBuilder(world.fillNewVehicles())
                .create(cx - 100, y + 48, 200, 20,
                        Component.translatable("gui.fcp.config.fill_new_vehicles"),
                        (button, newValue) -> sendWorld(SetWorldSettingPacket.Kind.FILL_NEW_VEHICLES, "", newValue ? 1 : 0));
        fill.active = worldEditable;
        addRenderableWidget(fill);

        CycleButton<Boolean> armor = CycleButton.onOffBuilder(world.armorModel())
                .create(cx - 100, y + 72, 200, 20,
                        Component.translatable("gui.fcp.config.armor_model"),
                        (button, newValue) -> sendWorld(SetWorldSettingPacket.Kind.ARMOR_MODEL, "", newValue ? 1 : 0));
        armor.active = worldEditable;
        armor.setTooltip(Tooltip.create(Component.translatable("gui.fcp.config.armor_model.tooltip")));
        addRenderableWidget(armor);

        // Row 5: world-wide artillery gravity multiplier.
        EditBox worldBox = multiplierBox(cx + 10, y + 96, world.artilleryWorld(), worldEditable);
        addRenderableWidget(setButton(cx + 64, y + 96, worldEditable,
                () -> sendMultiplier(SetWorldSettingPacket.Kind.ARTILLERY_WORLD, "", worldBox)));

        // Row 6: per-vehicle multiplier for the vehicle picked on the left.
        List<String> artillery = FcpWorldSettings.ARTILLERY;
        String vehicle = artillery.get(Math.floorMod(selectedArtillery, artillery.size()));
        CycleButton<String> picker = CycleButton.<String>builder(id -> EntityType.byString(id)
                        .map(EntityType::getDescription).orElse(Component.literal(id)))
                .withValues(artillery)
                .withInitialValue(vehicle)
                .displayOnlyValue()
                .create(cx - 100, y + 120, 106, 20, Component.empty(), (button, picked) -> {
                    selectedArtillery = artillery.indexOf(picked);
                    rebuildWidgets();
                });
        picker.active = !local;
        addRenderableWidget(picker);
        EditBox vehicleBox = multiplierBox(cx + 10, y + 120, world.artillery(vehicle), worldEditable);
        addRenderableWidget(setButton(cx + 64, y + 120, worldEditable,
                () -> sendMultiplier(SetWorldSettingPacket.Kind.ARTILLERY_VEHICLE, vehicle, vehicleBox)));

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(cx - 100, y + 170, 200, 20)
                .build());
    }

    /** Top of the widget column; the title sits above it. */
    private int top() {
        return this.height / 2 - 92;
    }

    private EditBox multiplierBox(int x, int y, float value, boolean active) {
        EditBox box = new EditBox(this.font, x, y, 50, 20, Component.translatable("gui.fcp.config.artillery_vehicle"));
        box.setValue(trim(value));
        box.setFilter(s -> s.matches("[0-9]*[.]?[0-9]*"));
        box.setEditable(active);
        addRenderableWidget(box);
        return box;
    }

    private Button setButton(int x, int y, boolean active, Runnable onPress) {
        Button button = Button.builder(Component.translatable("gui.fcp.config.set"), b -> onPress.run())
                .bounds(x, y, 36, 20).build();
        button.active = active;
        return button;
    }

    private static String trim(float value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.valueOf(value);
    }

    private void sendMultiplier(SetWorldSettingPacket.Kind kind, String vehicle, EditBox box) {
        try {
            sendWorld(kind, vehicle, Float.parseFloat(box.getValue()));
        } catch (NumberFormatException ignored) {
            // empty or a lone "." - nothing to send
        }
    }

    private void sendWorld(SetWorldSettingPacket.Kind kind, String vehicle, float value) {
        this.awaitingServer = true;
        FCPNetwork.FCP_HANDLER.sendToServer(new SetWorldSettingPacket(kind, vehicle, value));
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
        int cx = this.width / 2, y = top();
        graphics.drawCenteredString(this.font, this.title, cx, y - 20, 0xFFFFFF);
        Component worldLabel = Component.translatable("gui.fcp.config.artillery_world");
        graphics.drawString(this.font, worldLabel, cx + 4 - this.font.width(worldLabel), y + 102, 0xE0E0E0);

        Component status = statusLine();
        if (status != null) {
            graphics.drawCenteredString(this.font, status, cx, y + 146, 0xA0A0A0);
        }
        Component hint = inWorld() ? Component.translatable("gui.fcp.config.artillery_hint")
                : Component.translatable("gui.fcp.config.world_only");
        graphics.drawCenteredString(this.font, hint, cx, y + 156, 0x808080);
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