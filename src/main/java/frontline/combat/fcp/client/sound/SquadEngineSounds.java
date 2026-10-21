package frontline.combat.fcp.client.sound;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.engine.EngineControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Plays the layered Squad engine for every nearby vehicle with a {@link SquadEngineProfile}.
 *
 * Per vehicle it keeps RPM and Throttle the way Squad feeds its engine cue, then runs every layer
 * of the profile as its own looping sound whose volume and pitch are recomputed each tick from the
 * cue maths. Exterior layers are positional (mono files); interior layers play head-relative
 * (stereo files) and only one set is audible at a time - interior while the local player rides the
 * vehicle in first person, which is Squad's InVehicle branch. Start/stop one-shots play on the
 * engine state changes from {@link EngineControl}.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class SquadEngineSounds {

    private static final double RANGE = 128.0;
    /** Ticks over which the idle crossfades in at the end of a start sound. */
    private static final int IDLE_HANDOVER = 6;
    private static final SoundSource SOURCE = SoundSource.NEUTRAL;

    private static final Map<Integer, EngineAudio> ACTIVE = new HashMap<>();
    private static ClientLevel lastLevel;

    private SquadEngineSounds() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return;
        if (mc.level != lastLevel) {
            stopAll();
            lastLevel = mc.level;
        }
        if (mc.level == null || mc.player == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof VehicleEntity vehicle) || ACTIVE.containsKey(vehicle.getId())) continue;
            if (vehicle.distanceToSqr(mc.player) > RANGE * RANGE) continue;
            SquadEngineProfile profile = SquadEngineProfiles.get(EngineControl.id(vehicle));
            if (profile != null) ACTIVE.put(vehicle.getId(), new EngineAudio(vehicle, profile));
        }

        Iterator<EngineAudio> it = ACTIVE.values().iterator();
        while (it.hasNext()) {
            EngineAudio audio = it.next();
            if (!audio.tick(mc)) {
                audio.stop();
                it.remove();
            }
        }
    }

    public static void stopAll() {
        ACTIVE.values().forEach(EngineAudio::stop);
        ACTIVE.clear();
    }

    private static SoundEvent event(net.minecraft.resources.ResourceLocation id) {
        return SoundEvent.createVariableRangeEvent(id);
    }

    /** One vehicle's engine: Squad's RPM/Throttle parameters plus a loop per profile layer. */
    private static final class EngineAudio {
        final VehicleEntity vehicle;
        final SquadEngineProfile profile;
        final List<LayerSound> loops = new ArrayList<>();

        byte lastState;
        int stateTicks;
        /** 0..1 engine presence: fades in over the second half of the start, out on stop. */
        float level;
        float rpm, throttle;
        boolean inside;
        /** Which set the current loops belong to. */
        boolean loopsInside;
        SoundInstance startSound;
        /** Ticks since the start sound began, while the idle is still to take over; -1 otherwise. */
        int startClock = -1;
        /** Tick (of startClock) at which the start sound ends and the idle must be at full. */
        int startEnd;

        EngineAudio(VehicleEntity vehicle, SquadEngineProfile profile) {
            this.vehicle = vehicle;
            this.profile = profile;
            this.lastState = currentState();
            // Already running when it came into range: no start sound, straight to idle.
            this.level = lastState == EngineControl.OFF ? 0f : 1f;
            this.stateTicks = EngineControl.startTicks(vehicle);
        }

        /** @return false once this vehicle no longer needs audio. */
        boolean tick(Minecraft mc) {
            if (vehicle.isRemoved() || vehicle.distanceToSqr(mc.player) > RANGE * RANGE * 1.1) return false;

            inside = mc.player.getVehicle() == vehicle && mc.options.getCameraType().isFirstPerson();

            byte state = currentState();
            if (state != lastState) {
                if (state == EngineControl.STARTING || (state == EngineControl.RUNNING && lastState == EngineControl.OFF && !EngineControl.appliesTo(vehicle))) {
                    SquadEngineProfile.OneShot shot = inside ? profile.startInt : profile.startExt;
                    startSound = playOneShot(mc, shot);
                    startClock = 0;
                    startEnd = shot != null && shot.duration() > 0 ? Math.round(shot.duration() * 20f) : EngineControl.startTicks(vehicle);
                } else if (state == EngineControl.OFF && lastState == EngineControl.STARTING) {
                    // Key let go before the engine caught: cut the cranking, no shutdown sound.
                    if (startSound != null) mc.getSoundManager().stop(startSound);
                } else if (state == EngineControl.OFF) {
                    playOneShot(mc, inside ? profile.stopInt : profile.stopExt);
                }
                if (state != EngineControl.STARTING) startSound = null;
                lastState = state;
                stateTicks = 0;
            }
            stateTicks++;

            // Squad's start recordings end at full engine where the idle takes over, so the idle
            // crossfades in over the last IDLE_HANDOVER ticks of the start sound actually playing
            // (interior and exterior starts differ in length).
            float targetLevel;
            if (state == EngineControl.OFF) {
                targetLevel = 0f;
                startClock = -1;
            } else if (startClock >= 0) {
                startClock++;
                targetLevel = Mth.clamp((startClock - (startEnd - IDLE_HANDOVER)) / (float) IDLE_HANDOVER, 0f, 1f);
                if (startClock >= startEnd) startClock = -1;
            } else {
                targetLevel = 1f;
            }
            level = state == EngineControl.OFF ? approach(level, 0f, 0.125f) : targetLevel;

            boolean running = state == EngineControl.RUNNING;
            float targetThrottle = running && (vehicle.forwardInputDown() || vehicle.backInputDown()) ? 1f : 0f;
            throttle = approach(throttle, targetThrottle, targetThrottle > throttle ? 0.2f : 0.1f);

            float targetRpm = state == EngineControl.OFF ? 0f
                    : Math.max(speedFraction() * profile.rpmMax, throttle * profile.rpmLoad);
            rpm = approach(rpm, targetRpm, targetRpm > rpm ? profile.rpmRise : profile.rpmFall);

            // Only the audible set (interior or exterior) is kept playing, which halves the
            // channels each engine uses. On a switch the old set fades out and retires itself
            // while the new one fades in.
            if (!loops.isEmpty() && loopsInside != inside) {
                loops.forEach(LayerSound::retire);
                loops.clear();
            }
            if (level > 0f && loops.isEmpty()) {
                loopsInside = inside;
                for (SquadEngineProfile.Layer layer : profile.layers) {
                    if (layer.interior != inside) continue;
                    LayerSound sound = new LayerSound(this, layer);
                    loops.add(sound);
                    mc.getSoundManager().play(sound);
                }
            }
            if (level <= 0f && state == EngineControl.OFF && !loops.isEmpty()) {
                loops.forEach(LayerSound::finish);
                loops.clear();
            }
            return true;
        }

        /**
         * Engine state for the sound. Ground vehicles use EngineControl's start/stop; helicopters
         * run on SBW's own start-up, so they count as running while the rotor has any power.
         */
        byte currentState() {
            if (!profile.helicopter || EngineControl.appliesTo(vehicle)) return EngineControl.state(vehicle);
            return Math.abs(vehicle.getPower()) > 0.005f ? EngineControl.RUNNING : EngineControl.OFF;
        }

        /** Helicopters: rotor presence follows SBW's power, which ramps up through the start. */
        float rotorLevel() {
            return Mth.clamp(Math.abs(vehicle.getPower()) / 0.6f, 0f, 1f);
        }

        /** |power| as a fraction of the configured top power, which is what SBW drives speed from. */
        float speedFraction() {
            float power = vehicle.getPower();
            EngineInfo info = vehicle.getEngineInfo();
            float max = 1f;
            if (info instanceof EngineInfo.Wheel w) max = power >= 0 ? w.getMaxForwardSpeedRate() : w.getMaxBackwardSpeedRate();
            else if (info instanceof EngineInfo.Track t) max = power >= 0 ? t.getMaxForwardSpeedRate() : t.getMaxBackwardSpeedRate();
            return max <= 0f ? 0f : Mth.clamp(Math.abs(power) / max, 0f, 1f);
        }

        float targetVolume(SquadEngineProfile.Layer layer) {
            if (layer.interior != inside || level <= 0f) return 0f;
            float master = layer.interior ? profile.interiorVolume : profile.exteriorVolume;
            float v = level * master * layer.volume(rpm, throttle);
            if (!layer.interior && layer.distanceFade != null && Minecraft.getInstance().player != null) {
                v *= layer.distanceVolume((float) Minecraft.getInstance().player.distanceTo(vehicle));
            }
            return Mth.clamp(v, 0f, 1f);
        }

        float targetPitch(SquadEngineProfile.Layer layer) {
            float p = layer.pitch(rpm, throttle);
            if (profile.helicopter) p *= Mth.lerp(rotorLevel(), profile.rotorPitchMin, profile.rotorPitchMax);
            return Mth.clamp(p, 0.5f, 2f);
        }

        SoundInstance playOneShot(Minecraft mc, SquadEngineProfile.OneShot shot) {
            if (shot == null) return null;
            float master = inside ? profile.interiorVolume : profile.exteriorVolume;
            float volume = Mth.clamp(shot.volume() * master, 0f, 1f);
            SoundInstance instance = inside
                    ? new SimpleSoundInstance(shot.sound(), SOURCE, volume, 1f, RandomSource.create(), false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true)
                    : new SimpleSoundInstance(shot.sound(), SOURCE, volume, 1f, RandomSource.create(), false, 0,
                    SoundInstance.Attenuation.LINEAR, vehicle.getX(), vehicle.getY(), vehicle.getZ(), false);
            mc.getSoundManager().play(instance);
            return instance;
        }

        void stop() {
            loops.forEach(LayerSound::finish);
            loops.clear();
        }
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(target, value + step) : Math.max(target, value - step);
    }

    /** One looping layer; reads its volume/pitch from the owning EngineAudio every sound tick. */
    private static final class LayerSound extends AbstractTickableSoundInstance {
        private final EngineAudio audio;
        private final SquadEngineProfile.Layer layer;
        /** Left behind by a set switch: fades to silence, then stops. */
        private boolean retiring;

        LayerSound(EngineAudio audio, SquadEngineProfile.Layer layer) {
            super(event(layer.sound), SOURCE, SoundInstance.createUnseededRandom());
            this.audio = audio;
            this.layer = layer;
            this.looping = true;
            this.delay = 0;
            this.volume = 0f;
            this.pitch = audio.targetPitch(layer);
            if (layer.interior) {
                this.relative = true;
                this.attenuation = SoundInstance.Attenuation.NONE;
            } else {
                this.attenuation = SoundInstance.Attenuation.LINEAR;
                follow();
            }
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            if (audio.vehicle.isRemoved()) {
                stop();
                return;
            }
            if (!layer.interior) follow();
            // Short smoothing so set switches (in/out of the vehicle) and RPM steps don't click.
            volume = approach(volume, retiring ? 0f : audio.targetVolume(layer), 0.25f);
            pitch = approach(pitch, audio.targetPitch(layer), 0.1f);
            if (retiring && volume <= 0f) stop();
        }

        void retire() {
            retiring = true;
        }

        private void follow() {
            x = audio.vehicle.getX();
            y = audio.vehicle.getY();
            z = audio.vehicle.getZ();
        }

        void finish() {
            stop();
        }
    }
}
