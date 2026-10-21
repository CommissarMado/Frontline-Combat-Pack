package frontline.combat.fcp.client.sound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One vehicle's Squad weapon and turret sounds, as read from {@code assets/<ns>/squad_weapons/*.json}.
 *
 * Weapons follow Squad's looping fire cues: a burst plays its start layers on the first shot,
 * runs its loop layers (the per-shot samples pre-stitched into a seamless loop) while firing
 * and plays its tail layers once it stops. The shooter in first person hears the
 * 1P set, everyone else the 3P set, whose layers crossfade by distance (metres = blocks) exactly
 * like Squad's FixedDistanceCrossFade and arrive late by the speed of sound (DistanceDelay).
 *
 * The turret is Squad's traverse loop: volume and pitch are continuous modulators on the turret's
 * rotation speed, plus start/stop clicks the gunner hears.
 */
public final class SquadWeaponProfile {

    public final List<String> vehicles = new ArrayList<>();
    public final Map<String, Weapon> weapons = new HashMap<>();
    public final Turret turret;

    SquadWeaponProfile(JsonObject json) {
        for (JsonElement e : GsonHelper.getAsJsonArray(json, "vehicles")) vehicles.add(e.getAsString());
        JsonObject w = GsonHelper.getAsJsonObject(json, "weapons", new JsonObject());
        for (String key : w.keySet()) weapons.put(key, new Weapon(w.getAsJsonObject(key)));
        turret = json.has("turret") ? new Turret(json.getAsJsonObject("turret")) : null;
    }

    public static final class Weapon {
        public final float volume;
        /** Ticks without a shot before the burst ends and the tail plays. */
        public final int burstGap;
        public final List<Layer> start1p, loop1p, tail1p, start3p, loop3p, tail3p;

        Weapon(JsonObject o) {
            volume = GsonHelper.getAsFloat(o, "volume", 1f);
            burstGap = GsonHelper.getAsInt(o, "burst_gap", 5);
            JsonObject fp = GsonHelper.getAsJsonObject(o, "first_person", new JsonObject());
            JsonObject tp = GsonHelper.getAsJsonObject(o, "third_person", new JsonObject());
            start1p = layers(fp, "start");
            loop1p = layers(fp, "loop");
            tail1p = layers(fp, "tail");
            start3p = layers(tp, "start");
            loop3p = layers(tp, "loop");
            tail3p = layers(tp, "tail");
        }
    }

    private static List<Layer> layers(JsonObject parent, String key) {
        List<Layer> list = new ArrayList<>();
        if (parent.has(key)) for (JsonElement e : parent.getAsJsonArray(key)) list.add(new Layer(e.getAsJsonObject()));
        return list;
    }

    public static final class Layer {
        public final ResourceLocation sound;
        public final float gain;
        /** Distance crossfade in blocks: fade-in start/end, fade-out start/end. Null = always full. */
        public final float[] fade;
        /** Positional (mono). Non-spatial layers (stereo echoes, 1P) are faded by {@link #falloff}. */
        public final boolean spatial;
        public final float falloff;
        /** Fixed delay in seconds (Squad SoundNodeDelay). */
        public final float delay;

        Layer(JsonObject o) {
            sound = new ResourceLocation(GsonHelper.getAsString(o, "sound"));
            gain = GsonHelper.getAsFloat(o, "gain", 1f);
            if (o.has("fade")) {
                JsonArray a = o.getAsJsonArray("fade");
                fade = new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat(), a.get(3).getAsFloat()};
            } else {
                fade = null;
            }
            spatial = GsonHelper.getAsBoolean(o, "spatial", true);
            falloff = GsonHelper.getAsFloat(o, "falloff", 0f);
            delay = GsonHelper.getAsFloat(o, "delay", 0f);
        }

        /** Squad SQSoundNodeFixedDistanceCrossFade. */
        public float crossFade(float d) {
            if (fade == null) return 1f;
            float inStart = fade[0], inEnd = fade[1], outStart = fade[2], outEnd = fade[3];
            if (d < inStart) return 0f;
            if (d < inEnd) return (d - inStart) / (inEnd - inStart);
            if (d <= outStart) return 1f;
            if (d < outEnd) return 1f - (d - outStart) / (outEnd - outStart);
            return 0f;
        }
    }

    public static final class Turret {
        public final ResourceLocation loop;
        /** Converts the turret's rotation (degrees/second) into Squad's Speed parameter. */
        public final float speedScale;
        public final float[] pitchIn, pitchOut, volumeIn, volumeOut;
        public final float insideVolume, outsideVolume;
        /** Gunner's start/stop clicks; null on mounts that have none (open turrets). */
        public final Click start, stop;

        Turret(JsonObject o) {
            loop = new ResourceLocation(GsonHelper.getAsString(o, "loop"));
            speedScale = GsonHelper.getAsFloat(o, "speed_scale", 1f);
            JsonObject pm = GsonHelper.getAsJsonObject(o, "pitch_mod"), vm = GsonHelper.getAsJsonObject(o, "volume_mod");
            pitchIn = pair(pm, "in");
            pitchOut = pair(pm, "out");
            volumeIn = pair(vm, "in");
            volumeOut = pair(vm, "out");
            insideVolume = GsonHelper.getAsFloat(o, "inside_volume", 1f);
            outsideVolume = GsonHelper.getAsFloat(o, "outside_volume", 1f);
            start = Click.of(o, "start");
            stop = Click.of(o, "stop");
        }

        public record Click(ResourceLocation sound, float volume, float pitchMin, float pitchMax) {
            static Click of(JsonObject parent, String key) {
                if (!parent.has(key)) return null;
                JsonObject c = parent.getAsJsonObject(key);
                float[] p = c.has("pitch") ? pair(c, "pitch") : new float[]{1f, 1f};
                return new Click(new ResourceLocation(GsonHelper.getAsString(c, "sound")), GsonHelper.getAsFloat(c, "volume", 1f), p[0], p[1]);
            }
        }

        private static float[] pair(JsonObject o, String key) {
            JsonArray a = o.getAsJsonArray(key);
            return new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat()};
        }

        /** UE SoundNodeModulatorContinuous. */
        public static float modulate(float x, float[] in, float[] out) {
            if (in[1] <= in[0]) return out[0];
            float c = Math.max(in[0], Math.min(in[1], x));
            return out[0] + (c - in[0]) * (out[1] - out[0]) / (in[1] - in[0]);
        }
    }
}
