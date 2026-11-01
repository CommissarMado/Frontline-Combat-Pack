package frontline.combat.fcp.client.screen;

import com.mojang.logging.LogUtils;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.FCPConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * First-launch terms prompt, styled after vanilla's multiplayer safety screen.
 *
 * Shown in place of the first screen the game opens until the player ticks the box and
 * the proceed delay has run out. Acceptance is recorded as a hash of the Minecraft account
 * plus the OS user and machine name, so copying an instance (config included) to someone
 * else brings the prompt back for them.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DisclaimerScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();
    /** Bump to make everyone accept again after the terms change. */
    private static final String TERMS_VERSION = "v1";
    private static final long PROCEED_DELAY_MS = 3000;
    private static final int LINE_HEIGHT = 11;
    private static final int BOX_PAD = 4;
    private static final float TITLE_SCALE = 1.75f;
    private static final int TITLE_GAP = 12;
    private static final int CHECK_GAP = 10;
    private static final int BUTTON_GAP = 6;

    private static final Component TITLE = Component.translatable("gui.fcp.disclaimer.title").withStyle(ChatFormatting.BOLD);
    private static final Component MESSAGE = Component.translatable("gui.fcp.disclaimer.message");
    private static final Component CHECK = Component.translatable("gui.fcp.disclaimer.check");

    /** Cached so the file isn't re-read on every screen change once accepted. */
    private static boolean accepted;

    private final Screen next;
    /** When the box was last ticked; -1 while unticked. The proceed delay counts from here. */
    private long checkedAt = -1;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private float titleScale = TITLE_SCALE;
    private int titleY;
    private int textTop;
    private Checkbox agree;
    private Button proceed;

    public DisclaimerScreen(Screen next) {
        super(TITLE);
        this.next = next;
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        if (screen == null || screen instanceof DisclaimerScreen || accepted) return;
        if (!FCPConfig.noticeEnabled() || hasAccepted()) {
            accepted = true;
            return;
        }
        event.setNewScreen(new DisclaimerScreen(screen));
    }

    @Override
    protected void init() {
        int textWidth = Math.min(this.width - 50, 460);
        this.message = MultiLineLabel.create(this.font, MESSAGE, textWidth);
        // Title drawn larger than normal text, shrunk back down if it wouldn't fit the window.
        this.titleScale = Math.min(TITLE_SCALE, (this.width - 20) / (float) this.font.width(this.title));

        // Lay everything out as one block, centred vertically so it fits at any GUI scale.
        int titleHeight = Math.round(this.font.lineHeight * this.titleScale);
        int boxHeight = this.message.getLineCount() * LINE_HEIGHT + BOX_PAD * 2;
        int total = titleHeight + TITLE_GAP + boxHeight + CHECK_GAP + 20 + BUTTON_GAP + 20;
        this.titleY = Math.max(6, (this.height - total) / 2);
        this.textTop = this.titleY + titleHeight + TITLE_GAP + BOX_PAD;
        int checkY = this.textTop - BOX_PAD + boxHeight + CHECK_GAP;
        int buttonY = checkY + 20 + BUTTON_GAP;

        boolean wasChecked = this.agree != null && this.agree.selected();
        int checkWidth = this.font.width(CHECK) + 24;
        this.agree = this.addRenderableWidget(new Checkbox(this.width / 2 - checkWidth / 2, checkY,
                checkWidth, 20, CHECK, wasChecked));

        this.proceed = this.addRenderableWidget(Button.builder(CommonComponents.GUI_PROCEED, b -> accept())
                .bounds(this.width / 2 - 155, buttonY, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), b -> this.minecraft.stop())
                .bounds(this.width / 2 + 5, buttonY, 150, 20).build());
        updateProceed();
    }

    @Override
    public void tick() {
        updateProceed();
    }

    private void updateProceed() {
        if (!this.agree.selected()) {
            this.checkedAt = -1;
            this.proceed.setMessage(CommonComponents.GUI_PROCEED);
            this.proceed.active = false;
            return;
        }
        if (this.checkedAt < 0) this.checkedAt = Util.getMillis();
        long remaining = PROCEED_DELAY_MS - (Util.getMillis() - this.checkedAt);
        if (remaining > 0) {
            this.proceed.setMessage(Component.translatable("gui.fcp.disclaimer.proceed_wait", (remaining + 999) / 1000));
            this.proceed.active = false;
        } else {
            this.proceed.setMessage(CommonComponents.GUI_PROCEED);
            this.proceed.active = true;
        }
    }

    private void accept() {
        if (!this.agree.selected() || this.checkedAt < 0 || Util.getMillis() - this.checkedAt < PROCEED_DELAY_MS) return;
        recordAcceptance();
        accepted = true;
        this.minecraft.setScreen(this.next);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.pose().pushPose();
        graphics.pose().translate(this.width / 2f, this.titleY, 0);
        graphics.pose().scale(this.titleScale, this.titleScale, 1);
        graphics.drawCenteredString(this.font, this.title, 0, 0, 0xFFFFFF);
        graphics.pose().popPose();

        int textWidth = this.message.getWidth();
        int left = this.width / 2 - textWidth / 2;
        int top = this.textTop;
        int bottom = top + this.message.getLineCount() * LINE_HEIGHT;
        graphics.fill(left - BOX_PAD - 1, top - BOX_PAD - 1, left + textWidth + BOX_PAD + 1, bottom + BOX_PAD, 0xFFA0A0A0);
        graphics.fill(left - BOX_PAD, top - BOX_PAD, left + textWidth + BOX_PAD, bottom + BOX_PAD - 1, 0xFF000000);
        this.message.renderLeftAligned(graphics, left, top, LINE_HEIGHT, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(TITLE, MESSAGE);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    // --- acceptance record ---

    private static Path recordFile() {
        return FMLPaths.CONFIGDIR.get().resolve(".fcp_terms");
    }

    /** Ties acceptance to this account on this machine, not to the instance folder. */
    private static String fingerprint() {
        String host = System.getenv("COMPUTERNAME");
        if (host == null) host = System.getenv("HOSTNAME");
        String raw = String.join("|", "fcp-terms", TERMS_VERSION,
                String.valueOf(Minecraft.getInstance().getUser().getProfileId()),
                String.valueOf(System.getProperty("user.name")), String.valueOf(host));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return Integer.toHexString(raw.hashCode());
        }
    }

    private static boolean hasAccepted() {
        try {
            Path file = recordFile();
            return Files.exists(file) && Files.readAllLines(file).contains(fingerprint());
        } catch (Exception e) {
            return false;
        }
    }

    private static void recordAcceptance() {
        try {
            Path file = recordFile();
            Files.createDirectories(file.getParent());
            Files.writeString(file, fingerprint() + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            LOGGER.warn("Couldn't save FCP terms acceptance", e);
        }
    }
}
