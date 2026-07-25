package frontline.combat.fcp.init;

import com.atsuishio.superbwarfare.data.DataLoader;
import com.atsuishio.superbwarfare.data.IDBasedData;
import com.atsuishio.superbwarfare.data.vehicle.VehicleData;
import com.mojang.logging.LogUtils;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.FCPConfig;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reorganizes SBW's vehicle data map after it loads, enabling two things natively in
 * data/<ns>/sbw/vehicles/ with no datapack:
 *
 *   CREW VARIANTS  — single_crew/ and multicrew/ as the FIRST folder select per the
 *                    multicrew_vehicles config; the unselected side is dropped.
 *   CATEGORIES     — any other folders are organization only: tanks/bmp2.json loads as
 *                    if it were bmp2.json. Works inside the crew folders too
 *                    (multicrew/tanks/bmp2.json).
 *
 * How: SBW's loader already scans subfolders (FileToIdConverter is recursive) but keys
 * entries by their PATH ("fcp:tanks/bmp2"), which no entity registry id matches — so
 * subfoldered files load as dead entries. This listener re-keys them to their basename
 * ("fcp:bmp2") and applies the crew selection.
 *
 * Ordering is guaranteed, not hoped for: SBW's ComplexJsonResourceReloadListener does all
 * its loading in the PREPARE phase; this listener works in APPLY, and vanilla runs every
 * apply after all prepares complete. It also runs before OnDatapackSyncEvent, so the
 * remapped map is exactly what SBW syncs to clients.
 *
 * Precedence for one vehicle id: selected crew variant > categorized shared file > flat
 * file. Flat files (no folder) are untouched unless overridden — SBW's own vehicles and
 * any not-yet-moved FCP files keep working as-is.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public class VehicleDataReorganizer {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DIRECTORY = "sbw/vehicles";
    private static final String SINGLE_CREW = "single_crew";
    private static final String MULTICREW = "multicrew";
    private static final Object MARKER = new Object();

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Object>() {
            @Override
            protected Object prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return MARKER;
            }

            @Override
            protected void apply(Object prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
                reorganize();
            }
        });
    }

    private static void reorganize() {
        DataLoader.GeneralData<?> general = DataLoader.INSTANCE.getLOADED_DATA().get(DIRECTORY);
        if (general == null) return;
        HashMap<String, Object> map = general.getDataMap();

        boolean multicrew = FCPConfig.multicrewEnabled();
        String selected = multicrew ? MULTICREW : SINGLE_CREW;
        String rejected = multicrew ? SINGLE_CREW : MULTICREW;

        Map<String, Object> shared = new HashMap<>(); // category folders only
        Map<String, Object> crew = new HashMap<>();   // the selected crew folder
        List<String> pathedKeys = new ArrayList<>();
        int dropped = 0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            int colon = key.indexOf(':');
            String namespace = colon < 0 ? "minecraft" : key.substring(0, colon);
            String path = colon < 0 ? key : key.substring(colon + 1);

            int firstSlash = path.indexOf('/');
            if (firstSlash < 0) continue; // flat entry — leave untouched

            pathedKeys.add(key);
            String firstFolder = path.substring(0, firstSlash);
            String finalKey = namespace + ":" + path.substring(path.lastIndexOf('/') + 1);

            if (firstFolder.equals(selected)) {
                crew.put(finalKey, entry.getValue());
            } else if (firstFolder.equals(rejected)) {
                dropped++;
            } else {
                shared.put(finalKey, entry.getValue()); // organization-only folders
            }
        }

        if (pathedKeys.isEmpty()) return;

        pathedKeys.forEach(map::remove);
        map.putAll(shared);
        map.putAll(crew); // crew variant outranks a shared file for the same vehicle

        // The loader stamped each entry's id with its PATH; restore the id entities look up.
        map.forEach((key, value) -> {
            if (value instanceof IDBasedData<?> idData && !key.equals(idData.getId())) {
                idData.setId(key);
            }
        });

        // Same refresh SBW's own reload hook performs, so nothing serves stale data.
        VehicleData.dataCache.invalidateAll();

        LOGGER.info("[FCP] vehicle data reorganized: mode={}, {} crew-specific, {} categorized, {} dropped ({})",
                selected, crew.size(), shared.size(), dropped, rejected);
    }
}