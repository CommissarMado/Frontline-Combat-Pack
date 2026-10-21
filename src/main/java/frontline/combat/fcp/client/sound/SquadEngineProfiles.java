package frontline.combat.fcp.client.sound;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.engine.EngineControl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads {@code assets/<ns>/squad_engines/*.json} on every resource reload and publishes which
 * vehicles they cover to {@link EngineControl#SQUAD_SOUND_VEHICLES}, which mutes SBW's own engine
 * loop on those vehicles.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SquadEngineProfiles extends SimpleJsonResourceReloadListener {

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private static volatile Map<String, SquadEngineProfile> byVehicle = Map.of();

    private SquadEngineProfiles() {
        super(new Gson(), "squad_engines");
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SquadEngineProfiles());
    }

    public static SquadEngineProfile get(String vehicleId) {
        return byVehicle.get(vehicleId);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, SquadEngineProfile> map = new HashMap<>();
        files.forEach((id, json) -> {
            try {
                SquadEngineProfile profile = new SquadEngineProfile(json.getAsJsonObject());
                for (String vehicle : profile.vehicles) map.put(vehicle, profile);
            } catch (RuntimeException e) {
                LOGGER.error("Bad squad engine profile {}", id, e);
            }
        });
        byVehicle = Map.copyOf(map);
        EngineControl.SQUAD_SOUND_VEHICLES.clear();
        EngineControl.SQUAD_SOUND_VEHICLES.addAll(map.keySet());
        SquadEngineSounds.stopAll();
    }
}
