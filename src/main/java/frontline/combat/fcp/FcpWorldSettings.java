package frontline.combat.fcp;

import com.atsuishio.superbwarfare.data.gun.GunData;
import frontline.combat.fcp.network.SetMulticrewPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Per-world FCP settings, edited from the F8 screen by operators and saved with the world
 * (data/fcp_world_settings.dat):
 * <ul>
 *   <li>{@code lockInventories}: nobody can open any vehicle's inventory.</li>
 *   <li>{@code fillNewVehicles}: a newly spawned vehicle starts with its full combat load (VehicleRearm).</li>
 *   <li>{@code armorModel}: FCP's armour-vs-penetration damage (VehicleArmor); off = SBW's own damage
 *       modifiers, for packs that expect them. Off by default.</li>
 *   <li>Artillery gravity multipliers: the projectile gravity of each artillery vehicle's main gun
 *       is multiplied by its own value and by the world-wide one (range scales roughly with 1/x).
 *       Applied to the gun's GRAVITY property itself, so the shot, the HUD's firing solution and the
 *       artillery indicator all agree.</li>
 * </ul>
 * The live values sit in {@link #current()}: on the server from the saved data, on clients from
 * the state packet (they need the gravity for their own trajectory prediction).
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class FcpWorldSettings extends SavedData {

    private static final String NAME = "fcp_world_settings";

    /** Vehicles whose "Main" gun the gravity multipliers apply to. */
    public static final List<String> ARTILLERY = List.of(
            "fcp:m109", "fcp:msta", "fcp:2s31", "fcp:stryker_mortar", "fcp:toyota_hilux_mortar",
            "fcp:ural_grad", "fcp:toyota_hilux_rocket_pod");

    public record Snapshot(boolean lockInventories, boolean fillNewVehicles, boolean armorModel, float artilleryWorld, Map<String, Float> artillery) {
        public static final Snapshot DEFAULT = new Snapshot(false, false, false, 1f, Map.of());

        public float artillery(String vehicle) {
            return artillery.getOrDefault(vehicle, 1f);
        }
    }

    private static volatile Snapshot current = Snapshot.DEFAULT;

    private boolean lockInventories, fillNewVehicles;
    private boolean armorModel;
    private float artilleryWorld = 1f;
    private final Map<String, Float> artillery = new LinkedHashMap<>();

    public static Snapshot current() {
        return current;
    }

    /** Client side: the server's values from the state packet (DEFAULT again on leaving). */
    public static void acceptFromServer(Snapshot snapshot) {
        current = snapshot;
    }

    /** True (and tells the player) when the inventory lock is on. */
    public static boolean refuseInventory(Player player) {
        if (!current.lockInventories()) return false;
        player.displayClientMessage(Component.translatable("gui.fcp.config.inventories_locked"), true);
        return true;
    }

    // ---- server ----------------------------------------------------------------------------------

    public static FcpWorldSettings get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FcpWorldSettings::load, FcpWorldSettings::new, NAME);
    }

    public void setLockInventories(boolean lock) {
        lockInventories = lock;
        changed();
    }

    public void setFillNewVehicles(boolean fill) {
        fillNewVehicles = fill;
        changed();
    }

    public void setArmorModel(boolean on) {
        armorModel = on;
        changed();
    }

    public void setArtilleryWorld(float multiplier) {
        artilleryWorld = multiplier;
        changed();
    }

    public void setArtillery(String vehicle, float multiplier) {
        if (!ARTILLERY.contains(vehicle)) return;
        if (multiplier == 1f) artillery.remove(vehicle);
        else artillery.put(vehicle, multiplier);
        changed();
    }

    private void changed() {
        setDirty();
        publish();
    }

    private void publish() {
        current = new Snapshot(lockInventories, fillNewVehicles, armorModel, artilleryWorld, Collections.unmodifiableMap(new LinkedHashMap<>(artillery)));
    }

    private static FcpWorldSettings load(CompoundTag tag) {
        FcpWorldSettings s = new FcpWorldSettings();
        s.lockInventories = tag.getBoolean("LockInventories");
        s.fillNewVehicles = tag.getBoolean("FillNewVehicles");
        s.armorModel = tag.getBoolean("ArmorModel");
        s.artilleryWorld = tag.contains("ArtilleryWorld") ? tag.getFloat("ArtilleryWorld") : 1f;
        CompoundTag per = tag.getCompound("Artillery");
        for (String key : per.getAllKeys()) s.artillery.put(key, per.getFloat(key));
        return s;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("LockInventories", lockInventories);
        tag.putBoolean("FillNewVehicles", fillNewVehicles);
        tag.putBoolean("ArmorModel", armorModel);
        tag.putFloat("ArtilleryWorld", artilleryWorld);
        CompoundTag per = new CompoundTag();
        artillery.forEach(per::putFloat);
        tag.put("Artillery", per);
        return tag;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        get(event.getServer()).publish();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        current = Snapshot.DEFAULT;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) SetMulticrewPacket.sendStateTo(player);
    }

    // ---- artillery gravity ---------------------------------------------------------------------

    /** Main guns of artillery vehicles -> vehicle id. Weak: gun data dies with its vehicle. */
    private static final Map<GunData, String> ARTILLERY_GUNS = Collections.synchronizedMap(new WeakHashMap<>());

    public static void trackArtilleryGun(GunData main, String vehicle) {
        if (!ARTILLERY_GUNS.containsKey(main)) ARTILLERY_GUNS.put(main, vehicle);
    }

    /** Gravity multiplier for a gun: 1 unless it's an artillery main gun. */
    public static double gravityMultiplier(GunData data) {
        String vehicle = ARTILLERY_GUNS.get(data);
        if (vehicle == null) return 1.0;
        Snapshot s = current;
        return s.artilleryWorld() * s.artillery(vehicle);
    }
}
