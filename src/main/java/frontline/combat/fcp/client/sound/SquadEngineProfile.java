package frontline.combat.fcp.client.sound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * One vehicle's layered engine sound, as read from {@code assets/<ns>/squad_engines/*.json}.
 *
 * Mirrors the Squad SoundCue graph it was taken from: each layer is a looping wave with a gain,
 * a CrossFadeByParam window on RPM and any number of continuous modulators on volume and pitch,
 * multiplied together - the same maths UE uses, so the numbers can be copied from the SDK as-is.
 * Two parameters exist, as in Squad: {@code rpm} and {@code throttle}.
 */
public final class SquadEngineProfile {

    public final List<String> vehicles = new ArrayList<>();
    /** Helicopter rotors: running and volume follow SBW's power, pitch rises with it. */
    public final boolean helicopter;
    public final float rotorPitchMin, rotorPitchMax;
    public final float rpmMax, rpmLoad, rpmRise, rpmFall;
    public final float exteriorVolume, interiorVolume;
    public final OneShot startExt, startInt, stopExt, stopInt;
    public final List<Layer> layers = new ArrayList<>();

    SquadEngineProfile(JsonObject json) {
        for (JsonElement e : GsonHelper.getAsJsonArray(json, "vehicles")) vehicles.add(e.getAsString());

        JsonObject rpm = GsonHelper.getAsJsonObject(json, "rpm");
        rpmMax = GsonHelper.getAsFloat(rpm, "max");
        rpmLoad = GsonHelper.getAsFloat(rpm, "load", 0f);
        rpmRise = GsonHelper.getAsFloat(rpm, "rise");
        rpmFall = GsonHelper.getAsFloat(rpm, "fall");

        helicopter = GsonHelper.getAsString(json, "mode", "ground").equals("helicopter");
        JsonArray rp = json.has("rotor_pitch") ? json.getAsJsonArray("rotor_pitch") : null;
        rotorPitchMin = rp == null ? 1f : rp.get(0).getAsFloat();
        rotorPitchMax = rp == null ? 1f : rp.get(1).getAsFloat();
        exteriorVolume = GsonHelper.getAsFloat(json, "exterior_volume", 1f);
        interiorVolume = GsonHelper.getAsFloat(json, "interior_volume", 1f);

        JsonObject start = GsonHelper.getAsJsonObject(json, "start", new JsonObject());
        JsonObject stop = GsonHelper.getAsJsonObject(json, "stop", new JsonObject());
        startExt = OneShot.of(start, "exterior");
        startInt = OneShot.of(start, "interior");
        stopExt = OneShot.of(stop, "exterior");
        stopInt = OneShot.of(stop, "interior");

        for (JsonElement e : GsonHelper.getAsJsonArray(json, "layers")) layers.add(new Layer(e.getAsJsonObject()));
    }

    /** {@code duration}: length in seconds (0 if unknown); the idle takes over as a start sound ends. */
    public record OneShot(ResourceLocation sound, float volume, float duration) {
        static OneShot of(JsonObject parent, String key) {
            if (!parent.has(key)) return null;
            JsonObject o = parent.getAsJsonObject(key);
            return new OneShot(new ResourceLocation(GsonHelper.getAsString(o, "sound")),
                    GsonHelper.getAsFloat(o, "volume", 1f), GsonHelper.getAsFloat(o, "duration", 0f));
        }
    }

    public static final class Layer {
        public final ResourceLocation sound;
        public final boolean interior;
        public final float gain;
        /** CrossFadeByParam on RPM: fade-in start/end, fade-out start/end. Null = always full. */
        public final float[] fade;
        /** Parameter the fade window reads: RPM (usual) or Throttle. */
        public final boolean fadeOnThrottle;
        /** SoundNodeDistanceCrossFade window in blocks, exterior only. Null = none. */
        public final float[] distanceFade;
        public final List<Modulator> volume = new ArrayList<>();
        public final List<Modulator> pitch = new ArrayList<>();

        Layer(JsonObject o) {
            sound = new ResourceLocation(GsonHelper.getAsString(o, "sound"));
            interior = GsonHelper.getAsBoolean(o, "interior", false);
            gain = GsonHelper.getAsFloat(o, "gain", 1f);
            if (o.has("fade")) {
                JsonArray a = o.getAsJsonArray("fade");
                fade = new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat(), a.get(3).getAsFloat()};
            } else {
                fade = null;
            }
            fadeOnThrottle = GsonHelper.getAsString(o, "fade_param", "rpm").equalsIgnoreCase("throttle");
            if (o.has("distance_fade")) {
                JsonArray d = o.getAsJsonArray("distance_fade");
                distanceFade = new float[]{d.get(0).getAsFloat(), d.get(1).getAsFloat(), d.get(2).getAsFloat(), d.get(3).getAsFloat()};
            } else {
                distanceFade = null;
            }
            if (o.has("volume_mods")) for (JsonElement e : o.getAsJsonArray("volume_mods")) volume.add(new Modulator(e.getAsJsonObject()));
            if (o.has("pitch_mods")) for (JsonElement e : o.getAsJsonArray("pitch_mods")) pitch.add(new Modulator(e.getAsJsonObject()));
        }

        public float volume(float rpm, float throttle) {
            float v = gain * window(fade, fadeOnThrottle ? throttle : rpm);
            for (Modulator m : volume) v *= m.eval(rpm, throttle);
            return v;
        }

        public float pitch(float rpm, float throttle) {
            float p = 1f;
            for (Modulator m : pitch) p *= m.eval(rpm, throttle);
            return p;
        }

        /** UE SoundNodeParamCrossFade: ramps in over [inStart, inEnd], out over [outStart, outEnd]. */
        /** Volume at {@code distance} blocks from the listener (1 without a distance fade). */
        public float distanceVolume(float distance) {
            return window(distanceFade, distance);
        }

        private static float window(float[] fade, float x) {
            if (fade == null) return 1f;
            float inStart = fade[0], inEnd = fade[1], outStart = fade[2], outEnd = fade[3];
            if (x < inStart) return 0f;
            if (x < inEnd) return (x - inStart) / (inEnd - inStart);
            if (x <= outStart) return 1f;
            if (x < outEnd) return 1f - (x - outStart) / (outEnd - outStart);
            return 0f;
        }
    }

    /** UE SoundNodeModulatorContinuous: linear map of a clamped parameter. */
    public static final class Modulator {
        final boolean rpm, abs;
        final float minIn, maxIn, minOut, maxOut;

        Modulator(JsonObject o) {
            rpm = GsonHelper.getAsString(o, "param").equalsIgnoreCase("rpm");
            abs = GsonHelper.getAsBoolean(o, "abs", false);
            JsonArray in = o.getAsJsonArray("in"), out = o.getAsJsonArray("out");
            minIn = in.get(0).getAsFloat();
            maxIn = in.get(1).getAsFloat();
            minOut = out.get(0).getAsFloat();
            maxOut = out.get(1).getAsFloat();
        }

        float eval(float rpmValue, float throttle) {
            float x = rpm ? rpmValue : throttle;
            if (abs) x = Math.abs(x);
            if (maxIn <= minIn) return minOut;
            float c = Math.max(minIn, Math.min(maxIn, x));
            return minOut + (c - minIn) * (maxOut - minOut) / (maxIn - minIn);
        }
    }
}
