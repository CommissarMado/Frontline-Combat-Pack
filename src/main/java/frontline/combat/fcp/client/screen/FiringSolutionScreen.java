package frontline.combat.fcp.client.screen;

import frontline.combat.fcp.entity.vehicle.DelayedMortarVehicleBase;
import frontline.combat.fcp.entity.vehicle.IndirectFireVehicleBase;
import frontline.combat.fcp.firecontrol.FireControlComputation;
import frontline.combat.fcp.firecontrol.FireControlSolution;
import frontline.combat.fcp.firecontrol.FireControlStatus;
import frontline.combat.fcp.firecontrol.IndirectFireBallistics;
import frontline.combat.fcp.firecontrol.TrajectoryMode;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.message.SetFireControlMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;

public class FiringSolutionScreen extends Screen {

    private static final int PANEL_MAX_WIDTH = 380;
    private static final int PANEL_MAX_HEIGHT = 220;
    private static final int PANEL_MARGIN = 8;

    private static final int BACKGROUND = 0xF20C1012;
    private static final int SURFACE = 0xFF171D20;
    private static final int SURFACE_ALT = 0xFF20282C;
    private static final int BORDER = 0xFF49545A;
    private static final int TEXT = 0xFFE7ECEE;
    private static final int MUTED = 0xFF98A5AA;
    private static final int WARNING = 0xFFFFC857;
    private static final int ERROR = 0xFFFF665E;
    private static final int READY = 0xFF67D391;

    private final IndirectFireVehicleBase vehicle;
    private final Player player;

    private EditBox targetX;
    private EditBox targetY;
    private EditBox targetZ;
    private EditBox radius;
    private Button targetTab;
    private Button rangeTab;
    private Button lowMode;
    private Button highMode;
    private Button apply;
    private Button clear;
    private Button close;

    private TrajectoryMode trajectoryMode;
    private FireControlComputation preview = FireControlComputation.failure(FireControlStatus.INACTIVE);
    private boolean showRangeTable;
    private boolean compact;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public FiringSolutionScreen(IndirectFireVehicleBase vehicle, Player player) {
        super(Component.translatable("screen.fcp.fire_control.title"));
        this.vehicle = vehicle;
        this.player = player;
        this.trajectoryMode = vehicle.isFireControlActive()
                ? vehicle.getFireControlTrajectory()
                : vehicle instanceof DelayedMortarVehicleBase ? TrajectoryMode.HIGH : TrajectoryMode.LOW;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(PANEL_MAX_WIDTH, this.width - PANEL_MARGIN * 2);
        panelHeight = Math.min(PANEL_MAX_HEIGHT, this.height - PANEL_MARGIN * 2);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;
        compact = panelWidth < 350 || panelHeight < 200;

        int tabY = panelY + 24;
        targetTab = addRenderableWidget(Button.builder(
                Component.translatable("screen.fcp.fire_control.tab.solution"),
                button -> setRangeTable(false)
        ).bounds(panelX + 10, tabY, 86, 16).build());
        rangeTab = addRenderableWidget(Button.builder(
                Component.translatable("screen.fcp.fire_control.tab.range_table"),
                button -> setRangeTable(true)
        ).bounds(panelX + 100, tabY, 86, 16).build());

        if (compact) {
            createCompactInputs();
        } else {
            createWideInputs();
        }

        int actionY = panelY + panelHeight - 22;
        apply = addRenderableWidget(Button.builder(
                Component.translatable("screen.fcp.fire_control.apply"),
                button -> applySolution()
        ).bounds(panelX + 10, actionY, 70, 16).build());
        clear = addRenderableWidget(Button.builder(
                Component.translatable("screen.fcp.fire_control.clear"),
                button -> FCPNetwork.FCP_HANDLER.sendToServer(SetFireControlMessage.clear(vehicle.getId()))
        ).bounds(panelX + 84, actionY, 70, 16).build());
        close = addRenderableWidget(Button.builder(
                Component.translatable("screen.fcp.fire_control.close"),
                button -> onClose()
        ).bounds(panelX + panelWidth - 80, actionY, 70, 16).build());

        prefillInputs();
        updateModeLabels();
        updatePreview();
        updateWidgetVisibility();
    }

    private void createWideInputs() {
        int fieldX = panelX + 68;
        int fieldY = panelY + 52;
        targetX = createCoordinateBox(fieldX, fieldY, 88);
        targetY = createCoordinateBox(fieldX, fieldY + 22, 88);
        targetZ = createCoordinateBox(fieldX, fieldY + 44, 88);
        radius = createRadiusBox(fieldX, fieldY + 66, 44);

        lowMode = addRenderableWidget(Button.builder(Component.empty(), button -> setTrajectoryMode(TrajectoryMode.LOW))
                .bounds(panelX + 12, panelY + 146, 70, 16).build());
        highMode = addRenderableWidget(Button.builder(Component.empty(), button -> setTrajectoryMode(TrajectoryMode.HIGH))
                .bounds(panelX + 86, panelY + 146, 70, 16).build());
    }

    private void createCompactInputs() {
        int top = panelY + 51;
        int left = panelX + 28;
        int right = panelX + panelWidth / 2 + 12;
        targetX = createCoordinateBox(left, top, 86);
        targetY = createCoordinateBox(right, top, 86);
        targetZ = createCoordinateBox(left, top + 25, 86);
        radius = createRadiusBox(right, top + 25, 44);

        lowMode = addRenderableWidget(Button.builder(Component.empty(), button -> setTrajectoryMode(TrajectoryMode.LOW))
                .bounds(panelX + 10, panelY + 101, 68, 16).build());
        highMode = addRenderableWidget(Button.builder(Component.empty(), button -> setTrajectoryMode(TrajectoryMode.HIGH))
                .bounds(panelX + 82, panelY + 101, 68, 16).build());
    }

    private EditBox createCoordinateBox(int x, int y, int width) {
        EditBox box = new EditBox(this.font, x, y, width, 16, Component.empty());
        box.setMaxLength(10);
        box.setFilter(value -> value.matches("-?\\d*"));
        box.setBordered(false);
        box.setTextColor(TEXT);
        box.setResponder(value -> updatePreview());
        return addRenderableWidget(box);
    }

    private EditBox createRadiusBox(int x, int y, int width) {
        EditBox box = new EditBox(this.font, x, y, width, 16, Component.empty());
        box.setMaxLength(2);
        box.setFilter(value -> value.matches("\\d*"));
        box.setBordered(false);
        box.setTextColor(TEXT);
        box.setResponder(value -> updatePreview());
        return addRenderableWidget(box);
    }

    private void prefillInputs() {
        BlockPos target = vehicle.isFireControlActive() ? vehicle.getFireControlTarget() : getCrosshairTarget();
        targetX.setValue(Integer.toString(target.getX()));
        targetY.setValue(Integer.toString(target.getY()));
        targetZ.setValue(Integer.toString(target.getZ()));
        radius.setValue(Integer.toString(vehicle.isFireControlActive() ? vehicle.getFireControlRadius() : 0));

        if (!vehicle.isFireControlActive()) {
            FireControlComputation preferred = IndirectFireBallistics.solve(
                    vehicle, vehicle.getTurretControllerIndex(), target, trajectoryMode
            );
            if (!preferred.isSuccess()) {
                TrajectoryMode alternate = trajectoryMode == TrajectoryMode.LOW
                        ? TrajectoryMode.HIGH
                        : TrajectoryMode.LOW;
                if (IndirectFireBallistics.solve(
                        vehicle, vehicle.getTurretControllerIndex(), target, alternate
                ).isSuccess()) {
                    trajectoryMode = alternate;
                }
            }
        }
    }

    private BlockPos getCrosshairTarget() {
        HitResult hit = player.pick(512.0, 1.0f, false);
        return hit instanceof BlockHitResult blockHit
                ? blockHit.getBlockPos()
                : player.blockPosition();
    }

    private void setTrajectoryMode(TrajectoryMode mode) {
        trajectoryMode = mode;
        updateModeLabels();
        updatePreview();
    }

    private void updateModeLabels() {
        if (lowMode == null || highMode == null) {
            return;
        }
        lowMode.setMessage(Component.translatable("screen.fcp.fire_control.trajectory.low")
                .withStyle(trajectoryMode == TrajectoryMode.LOW ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.GRAY));
        highMode.setMessage(Component.translatable("screen.fcp.fire_control.trajectory.high")
                .withStyle(trajectoryMode == TrajectoryMode.HIGH ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.GRAY));
    }

    private void setRangeTable(boolean show) {
        showRangeTable = show;
        updateWidgetVisibility();
    }

    private void updateWidgetVisibility() {
        if (targetX == null) {
            return;
        }
        boolean inputsVisible = !showRangeTable;
        targetX.visible = inputsVisible;
        targetY.visible = inputsVisible;
        targetZ.visible = inputsVisible;
        radius.visible = inputsVisible;
        lowMode.visible = inputsVisible;
        highMode.visible = inputsVisible;
        apply.visible = inputsVisible;
        clear.visible = inputsVisible;
        targetTab.active = showRangeTable;
        rangeTab.active = !showRangeTable;
    }

    private void updatePreview() {
        if (targetX == null || targetY == null || targetZ == null || radius == null) {
            return;
        }
        try {
            int x = Integer.parseInt(targetX.getValue());
            int y = Integer.parseInt(targetY.getValue());
            int z = Integer.parseInt(targetZ.getValue());
            int hitRadius = Integer.parseInt(radius.getValue());
            if (hitRadius < 0 || hitRadius > IndirectFireBallistics.MAX_RADIUS) {
                preview = FireControlComputation.failure(FireControlStatus.INVALID_INPUT);
            } else {
                BlockPos target = new BlockPos(x, y, z);
                preview = y < vehicle.level().getMinBuildHeight()
                        || y >= vehicle.level().getMaxBuildHeight()
                        || !vehicle.level().getWorldBorder().isWithinBounds(target)
                        ? FireControlComputation.failure(FireControlStatus.INVALID_INPUT)
                        : IndirectFireBallistics.solve(
                                vehicle,
                                vehicle.getTurretControllerIndex(),
                                target,
                                trajectoryMode
                        );
            }
        } catch (NumberFormatException ignored) {
            preview = FireControlComputation.failure(FireControlStatus.INVALID_INPUT);
        }
        if (apply != null) {
            apply.active = preview.isSuccess();
            clear.active = vehicle.isFireControlActive();
        }
    }

    private void applySolution() {
        if (!preview.isSuccess()) {
            return;
        }
        try {
            BlockPos target = new BlockPos(
                    Integer.parseInt(targetX.getValue()),
                    Integer.parseInt(targetY.getValue()),
                    Integer.parseInt(targetZ.getValue())
            );
            int hitRadius = Integer.parseInt(radius.getValue());
            FCPNetwork.FCP_HANDLER.sendToServer(SetFireControlMessage.apply(
                    vehicle.getId(), target, hitRadius, trajectoryMode
            ));
        } catch (NumberFormatException ignored) {
            preview = FireControlComputation.failure(FireControlStatus.INVALID_INPUT);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (player.getVehicle() != vehicle) {
            onClose();
            return;
        }
        updatePreview();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int accent = 0xFF000000 | vehicle.getHudColor();

        graphics.fill(panelX + 2, panelY + 2, panelX + panelWidth + 2, panelY + panelHeight + 2, 0x99000000);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, BACKGROUND);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 2, accent);
        graphics.fill(panelX, panelY + 20, panelX + panelWidth, panelY + 21, BORDER);

        graphics.drawString(this.font, this.title, panelX + 10, panelY + 7, TEXT, false);
        String vehicleName = this.font.plainSubstrByWidth(vehicle.getDisplayName().getString(), panelWidth / 2);
        graphics.drawString(this.font, vehicleName,
                panelX + panelWidth - 10 - this.font.width(vehicleName), panelY + 7, MUTED, false);

        if (showRangeTable) {
            renderRangeTable(graphics, accent);
        } else if (compact) {
            renderCompactSolution(graphics, accent);
        } else {
            renderWideSolution(graphics, accent);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderWideSolution(GuiGraphics graphics, int accent) {
        int labelX = panelX + 12;
        int fieldY = panelY + 52;
        drawInputRow(graphics, Component.translatable("screen.fcp.fire_control.target_x"), labelX, fieldY, targetX);
        drawInputRow(graphics, Component.translatable("screen.fcp.fire_control.target_y"), labelX, fieldY + 22, targetY);
        drawInputRow(graphics, Component.translatable("screen.fcp.fire_control.target_z"), labelX, fieldY + 44, targetZ);
        drawInputRow(graphics, Component.translatable("screen.fcp.fire_control.radius"), labelX, fieldY + 66, radius);
        graphics.drawString(this.font, Component.translatable("screen.fcp.fire_control.trajectory"),
                labelX, panelY + 135, MUTED, false);

        int divider = panelX + 170;
        graphics.fill(divider, panelY + 45, divider + 1, panelY + panelHeight - 30, BORDER);
        renderSolutionReadout(graphics, divider + 12, panelY + 50, accent, true);
    }

    private void renderCompactSolution(GuiGraphics graphics, int accent) {
        graphics.drawString(this.font, "X", panelX + 12, panelY + 55, MUTED, false);
        graphics.drawString(this.font, "Y", panelX + panelWidth / 2 - 4, panelY + 55, MUTED, false);
        graphics.drawString(this.font, "Z", panelX + 12, panelY + 80, MUTED, false);
        graphics.drawString(this.font, "R", panelX + panelWidth / 2 - 4, panelY + 80, MUTED, false);
        drawInputSurface(graphics, targetX);
        drawInputSurface(graphics, targetY);
        drawInputSurface(graphics, targetZ);
        drawInputSurface(graphics, radius);
        renderSolutionReadout(graphics, panelX + panelWidth / 2 + 12, panelY + 104, accent, false);
    }

    private void drawInputRow(GuiGraphics graphics, Component label, int x, int y, EditBox box) {
        graphics.drawString(this.font, label, x, y + 4, MUTED, false);
        drawInputSurface(graphics, box);
    }

    private void drawInputSurface(GuiGraphics graphics, EditBox box) {
        graphics.fill(box.getX() - 2, box.getY() - 1,
                box.getX() + box.getWidth() + 2, box.getY() + box.getHeight() + 1, SURFACE_ALT);
        graphics.fill(box.getX() - 2, box.getY() + box.getHeight(),
                box.getX() + box.getWidth() + 2, box.getY() + box.getHeight() + 1,
                box.isFocused() ? READY : BORDER);
    }

    private void renderSolutionReadout(GuiGraphics graphics, int x, int y, int accent, boolean drawPlot) {
        FireControlStatus status = displayStatus();
        int statusColor = statusColor(status, accent);
        graphics.drawString(this.font, Component.translatable("screen.fcp.fire_control.status"), x, y, MUTED, false);
        graphics.drawString(this.font, Component.translatable(status.translationKey()), x + 48, y, statusColor, false);

        if (!preview.isSuccess()) {
            graphics.drawString(this.font, Component.translatable(preview.status().translationKey()),
                    x, y + 18, ERROR, false);
            return;
        }

        FireControlSolution solution = preview.solution();
        if (!drawPlot) {
            drawValue(graphics, x, y + 17, "screen.fcp.fire_control.range_value",
                    format1(solution.range()) + " m");
            drawValue(graphics, x, y + 27, "screen.fcp.fire_control.elevation_value",
                    format1(solution.pitch()) + "\u00B0");
            return;
        }
        drawValue(graphics, x, y + 18, "screen.fcp.fire_control.range_value",
                format1(solution.range()) + " m");
        drawValue(graphics, x, y + 32, "screen.fcp.fire_control.bearing_value",
                format1(solution.yaw()) + "\u00B0");
        drawValue(graphics, x, y + 46, "screen.fcp.fire_control.elevation_value",
                format1(solution.pitch()) + "\u00B0");
        drawValue(graphics, x, y + 60, "screen.fcp.fire_control.flight_time_value",
                format1(solution.flightTime() / 20.0) + " s");

        if (drawPlot) {
            drawTargetPlot(graphics, x + 123, y + 107, 27, accent);
        }
    }

    private FireControlStatus displayStatus() {
        if (vehicle.isFireControlActive()
                && parsedTarget().equals(vehicle.getFireControlTarget())
                && parsedRadius() == vehicle.getFireControlRadius()
                && trajectoryMode == vehicle.getFireControlTrajectory()) {
            return vehicle.getFireControlStatus();
        }
        return preview.isSuccess() ? FireControlStatus.ALIGNING : preview.status();
    }

    private BlockPos parsedTarget() {
        try {
            return new BlockPos(
                    Integer.parseInt(targetX.getValue()),
                    Integer.parseInt(targetY.getValue()),
                    Integer.parseInt(targetZ.getValue())
            );
        } catch (NumberFormatException ignored) {
            return BlockPos.ZERO;
        }
    }

    private int parsedRadius() {
        try {
            return Integer.parseInt(radius.getValue());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private void drawValue(GuiGraphics graphics, int x, int y, String key, String value) {
        graphics.drawString(this.font, Component.translatable(key), x, y, MUTED, false);
        graphics.drawString(this.font, value, x + 62, y, TEXT, false);
    }

    private void drawTargetPlot(GuiGraphics graphics, int centerX, int centerY, int plotRadius, int accent) {
        graphics.fill(centerX - plotRadius - 3, centerY - plotRadius - 3,
                centerX + plotRadius + 4, centerY + plotRadius + 4, SURFACE);
        for (int i = 0; i < 32; i++) {
            double angle = i * Math.PI * 2.0 / 32.0;
            int x = centerX + (int) Math.round(Math.cos(angle) * plotRadius);
            int y = centerY + (int) Math.round(Math.sin(angle) * plotRadius);
            graphics.fill(x, y, x + 1, y + 1, accent);
        }
        graphics.fill(centerX - 4, centerY, centerX + 5, centerY + 1, TEXT);
        graphics.fill(centerX, centerY - 4, centerX + 1, centerY + 5, TEXT);
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.fcp.fire_control.radius_short", radius.getValue()),
                centerX, centerY + plotRadius + 6, MUTED);
    }

    private void renderRangeTable(GuiGraphics graphics, int accent) {
        int startY = panelY + 48;
        int left = panelX + 18;
        int col2 = panelX + panelWidth / 2 - 15;
        int col3 = panelX + panelWidth - 86;
        graphics.drawString(this.font, Component.translatable("screen.fcp.fire_control.table.elevation"),
                left, startY, accent, false);
        graphics.drawString(this.font, Component.translatable("screen.fcp.fire_control.table.range"),
                col2, startY, accent, false);
        graphics.drawString(this.font, Component.translatable("screen.fcp.fire_control.table.time"),
                col3, startY, accent, false);
        graphics.fill(left, startY + 11, panelX + panelWidth - 18, startY + 12, BORDER);

        int rows = compact ? 6 : IndirectFireBallistics.RANGE_TABLE_ROWS;
        double minPitch = vehicle.getTurretMinPitch();
        double maxPitch = vehicle.getTurretMaxPitch();
        double velocity = vehicle.getProjectileVelocity(vehicle.getTurretControllerIndex());
        double gravity = vehicle.getProjectileGravity(vehicle.getTurretControllerIndex());
        double muzzleY = vehicle.getShootPos(vehicle.getTurretControllerIndex(), 1.0f).y;
        double targetY = parsedTarget().equals(BlockPos.ZERO)
                ? vehicle.getY()
                : parsedTarget().getCenter().y;
        int rowHeight = compact ? 11 : 15;

        for (int i = 0; i < rows; i++) {
            double pitch = minPitch + (maxPitch - minPitch) * i / (rows - 1.0);
            double range = IndirectFireBallistics.rangeAtPitch(velocity, gravity, muzzleY, targetY, pitch);
            double horizontalSpeed = velocity * Math.cos(Math.toRadians(pitch));
            double time = horizontalSpeed > 1.0E-8 ? range / horizontalSpeed / 20.0 : 0;
            int rowY = startY + 17 + i * rowHeight;
            if ((i & 1) == 0) {
                graphics.fill(left - 4, rowY - 2, panelX + panelWidth - 18, rowY + 9, SURFACE);
            }
            graphics.drawString(this.font, format1(pitch) + "\u00B0", left, rowY, TEXT, false);
            graphics.drawString(this.font, format0(range) + " m", col2, rowY, TEXT, false);
            graphics.drawString(this.font, format1(time) + " s", col3, rowY, TEXT, false);
        }
    }

    private static int statusColor(FireControlStatus status, int accent) {
        return switch (status) {
            case READY -> READY;
            case MOVING -> WARNING;
            case ALIGNING -> accent;
            case INACTIVE -> MUTED;
            case OUT_OF_RANGE, PITCH_LIMIT, YAW_LIMIT, INVALID_INPUT, INVALID_WEAPON, WRECKED -> ERROR;
        };
    }

    private static String format0(double value) {
        return String.format(Locale.ROOT, "%.0f", value);
    }

    private static String format1(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 257 || keyCode == 335) && apply.active && apply.visible) {
            applySolution();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
