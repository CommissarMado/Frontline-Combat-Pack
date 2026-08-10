package frontline.combat.fcp.client;

import com.mojang.logging.LogUtils;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.screen.VehicleInventoryScreen;
import frontline.combat.fcp.entity.vehicle.VehicleInventory;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.OpenVehicleHoldPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

/**
 * While riding an FCP vehicle that has a hold, opening your inventory opens the VEHICLE'S
 * hold instead of your own inventory.
 *
 * NOT tied to any key. It never looks at a keybind — it watches for the player-inventory
 * SCREEN appearing, so it works with the inventory key rebound to anything, and also catches
 * the inventory being opened by any other means.
 *
 * The screen it replaces is the vanilla inventory AND SBW's own vehicle container screen —
 * SBW claims the inventory key while you're riding and opens that instead of the vanilla
 * inventory, which is why matching only the vanilla one never fired.
 *
 * Two layers:
 *   1. {@link #onScreenOpening} vetoes it as it opens (clean path);
 *   2. {@link #onClientTick} is a safety net — anything that slips through is closed on the
 *      next tick. A tick always fires.
 * The hold itself is opened SEPARATELY by a server packet, never by faking a vehicle click
 * (which is what used to make mounting open the hold too).
 *
 * Registration is attempted TWO ways, guarded so it only happens once:
 *   - the @EventBusSubscriber below (if Forge's annotation scanning picks FCP classes up);
 *   - an explicit {@link #register()} you can call from the FCP main class.
 * Whichever runs first wins. If NEITHER logs "handler registered" at startup, nothing in this
 * class is running and that's the problem to chase, not the redirect logic.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class VehicleInventoryKeyHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Logs why a redirect didn't happen. Left ON while this is being brought up — it only
     * prints when you open your inventory and it declines to redirect. Set false once happy.
     */
    public static boolean DEBUG = false;

    private static boolean registered = false;

    /**
     * Ticks left to wait for the server to answer a redirect. While this is non-zero the
     * handler does nothing at all — without it the tick backstop re-fires every tick during
     * the round trip, closing and re-requesting in a loop.
     */
    private int pending = 0;

    /** How long to wait for the hold to arrive before allowing another attempt. */
    private static final int PENDING_TICKS = 40; // ~2 seconds

    private VehicleInventoryKeyHandler() {
    }

    /** Annotation path — runs if Forge scanning finds this class. */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(VehicleInventoryKeyHandler::register);
    }

    /**
     * Explicit path — safe to call from the FCP main class constructor or client setup.
     * Guarded, so calling it as well as the annotation firing is harmless.
     */
    public static void register() {
        if (registered) return;
        registered = true;
        MinecraftForge.EVENT_BUS.register(new VehicleInventoryKeyHandler());
        LOGGER.info("[FCP] vehicle hold inventory-redirect handler registered");
    }

    @SubscribeEvent
    public void onScreenOpening(ScreenEvent.Opening event) {
        // Our own hold arriving means the redirect finished — clear the wait and never
        // inspect it further.
        if (event.getNewScreen() instanceof VehicleInventoryScreen) {
            this.pending = 0;
            return;
        }
        if (this.pending > 0) return; // a redirect is already in flight
        if (!isRedirectableScreen(event.getNewScreen())) return;
        if (!shouldRedirect()) return;
        event.setCanceled(true);
        openHold();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();

        // The hold is open: nothing to do, and stop checking entirely. Re-examining the
        // screen we just opened is what made this loop.
        if (mc.screen instanceof VehicleInventoryScreen) {
            this.pending = 0;
            return;
        }
        // Waiting on the server — don't close anything or ask again.
        if (this.pending > 0) {
            this.pending--;
            return;
        }

        if (!isRedirectableScreen(mc.screen)) return;
        if (!shouldRedirect()) return;
        mc.setScreen(null);
        openHold();
    }

    /**
     * Is this the player's OWN inventory? Checked by menu identity rather than screen class,
     * so custom/modded inventory screens are caught too. The vehicle hold uses its own menu,
     * so it never matches here — no loop.
     */
    /**
     * Screens we replace with the vehicle's hold.
     *
     * This is NOT just the vanilla inventory. SBW claims the inventory key while you're
     * riding and opens its OWN vehicle container (Mini/Small/Medium/Large/Huge
     * VehicleContainerScreen) — that was the screen appearing all along, which is why every
     * "is this the player inventory" test correctly said no and nothing redirected.
     *
     * SBW's screens are matched by class NAME rather than by importing them: the 0.8.9 jar
     * doesn't necessarily expose the same class hierarchy as current SBW source, and a name
     * check can't break on that drift.
     */
    private static boolean isRedirectableScreen(Screen screen) {
        if (screen == null) return false;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;

        // Survival inventory.
        if (screen instanceof InventoryScreen) return true;
        // Creative inventory — its menu is a separate ItemPickerMenu, not player.inventoryMenu,
        // so the identity check below would miss it.
        if (screen instanceof CreativeModeInventoryScreen) return true;
        // SBW's own vehicle container, in any of its sizes.
        if (isSbwVehicleContainer(screen)) return true;
        // Anything else backed by the player's own menu — catches modded replacements.
        if (screen instanceof AbstractContainerScreen<?> container
                && container.getMenu() == player.inventoryMenu) {
            return true;
        }

        // Not recognised. Say so when we're aboard a hold, so an unexpected screen class can
        // never fail silently.
        if (DEBUG && findVehicle(player) != null) {
            LOGGER.info("[FCP] screen not redirected (unrecognised): {}",
                    screen.getClass().getName());
        }
        return false;
    }

    /** SBW's vehicle container screens, matched by name so SBW version drift can't break it. */
    private static boolean isSbwVehicleContainer(Screen screen) {
        String name = screen.getClass().getName();
        return name.startsWith("com.atsuishio.superbwarfare.")
                && name.endsWith("VehicleContainerScreen");
    }

    /** Riding an FCP vehicle that has a hold to show? */
    private static boolean shouldRedirect() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;

        VehicleInventory vehicle = findVehicle(player);
        if (vehicle == null) {
            // Riding nothing at all is the ordinary case for opening your inventory on foot,
            // so stay quiet — logging it just buries the line that actually matters. Only
            // speak up when you ARE aboard something and it still isn't recognised.
            Entity raw = player.getVehicle();
            if (DEBUG && raw != null) {
                LOGGER.info("[FCP] no redirect: riding {} which is not an FCP vehicle (root={})",
                        raw.getClass().getName(),
                        player.getRootVehicle().getClass().getName());
            }
            return false;
        }
        if (!vehicle.opensVehicleScreen()) {
            if (DEBUG) {
                LOGGER.info("[FCP] no redirect: {} has no hold (style={}, size={})",
                        vehicle.getClass().getSimpleName(), vehicle.inventoryStyle(),
                        vehicle.inventorySize());
            }
            return false;
        }
        // Deliberately NOT gated on being the driver: any occupant may open the hold. The
        // driver check kept resolving wrong on the client and blocking this entirely.
        return true;
    }

    /** The FCP vehicle being ridden, directly or through anything it's attached to. */
    private static VehicleInventory findVehicle(Player player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof VehicleInventory camo) return camo;
        // Fall back to the root, in case anything ever seats players on a proxy entity.
        Entity root = player.getRootVehicle();
        return root instanceof VehicleInventory camo ? camo : null;
    }

    private void openHold() {
        this.pending = PENDING_TICKS;
        FCPNetwork.FCP_HANDLER.sendToServer(new OpenVehicleHoldPacket());
    }
}