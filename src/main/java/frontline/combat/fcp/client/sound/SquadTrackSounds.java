package frontline.combat.fcp.client.sound;

import com.atsuishio.superbwarfare.client.sound.VehicleSoundInstance;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.FCP;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Replaces SBW's one track sound (VehicleEntity.playTrackSound) with Squad's, per vehicle family: every layer of the family's
 * Squad tracks cue with its own gain and speed curves. Each layer is one looping recording (the cues don't randomise or
 * cycle); interior layers play while the local player rides in first person, Squad's InVehicle branch. Squad's Speed
 * parameter is cm/s. The Leopard's tracks recording is thin and squeaky on its own: Squad plays it over a louder rumble.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class SquadTrackSounds {

    private static boolean installed;

    private SquadTrackSounds() {
    }

    /** One cue layer: volume = gain x 0..1 over Speed volFrom..volTo, pitch = pitchMin..pitchMax over Speed 0..pitchTo. */
    private record Layer(String sound, boolean interior, float gain, float volFrom, float volTo, float pitchTo, float pitchMin, float pitchMax) {
    }

    private static Layer ext(String sound, float gain, float volFrom, float volTo, float pitchTo, float pitchMin, float pitchMax) {
        return new Layer(sound, false, gain, volFrom, volTo, pitchTo, pitchMin, pitchMax);
    }

    private static Layer in(String sound, float gain, float volTo, float pitchTo, float pitchMin, float pitchMax) {
        return new Layer(sound, true, gain, 0f, volTo, pitchTo, pitchMin, pitchMax);
    }

    // Abrams_engine_tracks_Cue
    private static final List<Layer> ABRAMS = List.of(
            ext("abrams", 1.5f, 1, 500, 500, 0.4f, 1f),
            in("mtlb_int", 0.3f, 1000, 3000, 0.8f, 1.3f),
            in("speed_int", 0.7f, 1000, 3000, 0.8f, 1f));
    // Leopard_engine_speed_tracks_Cue; the rumble's pitch follows RPM in the cue and is at its top as soon as the tank moves
    private static final List<Layer> LEOPARD = List.of(
            ext("leopard", 1.1f, 0, 1000, 3000, 0.8f, 1.3f),
            ext("leopard_speed", 2.25f, 0, 2500, 100, 0.7f, 1.2f),
            in("mtlb_int", 0.6f, 1000, 3000, 0.8f, 1.3f),
            in("leopard_speed_int", 1.5f, 2500, 100, 0.7f, 1.2f));
    // T90_tracks_Cue
    private static final List<Layer> T90 = List.of(
            ext("t90", 1.5f, 1, 500, 500, 0.5f, 1.2f),
            in("mtlb_int", 0.315f, 1000, 3000, 0.8f, 1.3f),
            in("speed_int", 1f, 1000, 3000, 0.8f, 1f));
    // BMP1_engine_tracks_Cue
    private static final List<Layer> BMP = List.of(
            ext("bmp1", 1.5f, 1, 500, 500, 0.5f, 1.2f),
            in("bradley_int", 0.18f, 1000, 3000, 0.8f, 1.1f),
            in("speed_int", 0.4f, 1000, 3000, 0.8f, 1f));
    // Bradley_engine_tracks_Cue
    private static final List<Layer> BRADLEY = List.of(
            ext("bradley", 0.5f, 1, 500, 500, 0.5f, 1.2f),
            in("bradley_int", 0.45f, 1000, 3000, 0.8f, 1.1f));
    // Warrior_engine_tracks_Cue
    private static final List<Layer> WARRIOR = List.of(
            ext("warrior", 1.5f, 1, 500, 500, 0.4f, 1f),
            in("mtlb_int", 0.75f, 1000, 3000, 0.64f, 1.04f));
    // FV432_engine_tracks_Cue
    private static final List<Layer> FV432 = List.of(
            ext("fv432", 0.4f, 0, 200, 800, 0.4f, 1f),
            in("mtlb_int", 0.315f, 1000, 3000, 0.8f, 1.3f));

    private static List<Layer> familyOf(VehicleEntity vehicle) {
        String id = EntityType.getKey(vehicle.getType()).getPath();
        if (id.startsWith("leopard")) return LEOPARD;
        if (id.startsWith("t72") || id.startsWith("t90") || id.startsWith("t14") || id.equals("msta")) return T90;
        if (id.startsWith("bmp") || id.startsWith("brm") || id.equals("khrizantema") || id.equals("2s31")) return BMP;
        if (id.startsWith("m2a") || id.equals("m6a2") || id.equals("aavp") || id.equals("m109")) return BRADLEY;
        if (id.startsWith("puma") || id.equals("marder1a3")) return WARRIOR;
        if (id.startsWith("m113") || id.equals("skif")) return FV432;
        return ABRAMS; // the M1s, the Type 10, and any other tracked vehicle
    }

    /** SBW sets its consumer during its own init, so ours goes in on the first client tick. */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (installed || event.phase != TickEvent.Phase.END) return;
        installed = true;
        VehicleEntity.playTrackSound = vehicle -> {
            var sounds = Minecraft.getInstance().getSoundManager();
            for (Layer layer : familyOf(vehicle)) sounds.play(new Loop(vehicle, layer));
        };
    }

    private static final class Loop extends VehicleSoundInstance {
        private final Layer layer;

        Loop(VehicleEntity vehicle, Layer layer) {
            super(SoundEvent.createVariableRangeEvent(new ResourceLocation(FCP.MODID, "squad.tracks." + layer.sound())), Minecraft.getInstance(), vehicle);
            this.layer = layer;
        }

        /** Squad's Speed parameter: blocks per tick to cm/s. */
        private static float speed(VehicleEntity vehicle) {
            return (float) vehicle.getDeltaMovement().horizontalDistance() * 2000f;
        }

        @Override
        protected boolean canPlay(VehicleEntity vehicle) {
            return vehicle.engineRunning();
        }

        @Override
        protected float getPitch(VehicleEntity vehicle) {
            return Mth.lerp(Mth.clamp(speed(vehicle) / layer.pitchTo(), 0f, 1f), layer.pitchMin(), layer.pitchMax());
        }

        @Override
        protected float getVolume(VehicleEntity vehicle) {
            Minecraft mc = Minecraft.getInstance();
            boolean inside = mc.player != null && mc.player.getVehicle() == vehicle && mc.options.getCameraType().isFirstPerson();
            if (inside != layer.interior()) return 0f;
            return Math.min(1f, layer.gain() * Mth.clamp((speed(vehicle) - layer.volFrom()) / (layer.volTo() - layer.volFrom()), 0f, 1f));
        }
    }
}
