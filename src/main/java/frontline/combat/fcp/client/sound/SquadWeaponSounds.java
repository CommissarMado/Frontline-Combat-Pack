package frontline.combat.fcp.client.sound;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.engine.EngineControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Plays Squad weapon fire cues and turret traverse for vehicles with a {@link SquadWeaponProfile}.
 * Shots arrive from the server one by one (SquadFirePacket); bursts and the turret are tracked here.
 */
public final class SquadWeaponSounds {

    private static final Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final SoundSource SOURCE = SoundSource.PLAYERS;
    /** Speed of sound in blocks per tick, for Squad's DistanceDelay on 3P layers. */
    private static final float SOUND_SPEED = 343f / 20f;
    private static final double TURRET_RANGE = 64.0;

    private static volatile Map<String, SquadWeaponProfile> byVehicle = Map.of();
    private static final Map<String, Burst> BURSTS = new HashMap<>();
    private static final Map<Integer, TurretAudio> TURRETS = new HashMap<>();
    private static ClientLevel lastLevel;

    private SquadWeaponSounds() {
    }

    // ── Profiles ────────────────────────────────────────────────────────────────

    @Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Loader extends SimpleJsonResourceReloadListener {
        private Loader() {
            super(new Gson(), "squad_weapons");
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new Loader());
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, SquadWeaponProfile> map = new HashMap<>();
            files.forEach((id, json) -> {
                try {
                    SquadWeaponProfile profile = new SquadWeaponProfile(json.getAsJsonObject());
                    for (String vehicle : profile.vehicles) map.put(vehicle, profile);
                } catch (RuntimeException e) {
                    LOGGER.error("Bad squad weapon profile {}", id, e);
                }
            });
            byVehicle = Map.copyOf(map);
            stopAll();
        }
    }

    private static void stopAll() {
        TURRETS.values().forEach(t -> t.loop.end());
        TURRETS.clear();
        BURSTS.values().forEach(b -> b.loops.forEach(LoopSound::end));
        BURSTS.clear();
    }

    // ── Weapons ─────────────────────────────────────────────────────────────────

    /**
     * One burst of one weapon on one vehicle.
     *
     * Squad's fire cues don't play a sample per bullet: their per-shot samples are each exactly one
     * firing cycle long and are stitched back to back, sample-accurate, while the trigger is held.
     * Minecraft can only start sounds on whole ticks, so per-shot playback left gaps of silence.
     * Instead the samples are pre-stitched into a seamless loop (squad/.../*_loop.ogg) that runs for
     * the length of the burst; the tail plays when it ends.
     */
    private static final class Burst {
        final int vehicleId;
        final SquadWeaponProfile.Weapon weapon;
        final boolean firstPerson;
        final List<LoopSound> loops = new java.util.ArrayList<>();
        long lastShot;

        Burst(int vehicleId, SquadWeaponProfile.Weapon weapon, boolean firstPerson) {
            this.vehicleId = vehicleId;
            this.weapon = weapon;
            this.firstPerson = firstPerson;
        }

        void start(Minecraft mc, VehicleEntity vehicle) {
            playLayers(mc, vehicle, weapon, firstPerson ? weapon.start1p : weapon.start3p, firstPerson);
            int delay = firstPerson ? 0 : soundDelay(mc, vehicle);
            for (SquadWeaponProfile.Layer layer : firstPerson ? weapon.loop1p : weapon.loop3p) {
                LoopSound loop = new LoopSound(vehicle, weapon, layer, firstPerson);
                loops.add(loop);
                if (delay > 0) mc.getSoundManager().playDelayed(loop, delay);
                else mc.getSoundManager().play(loop);
            }
        }

        /** Stops the loops (as late as the sound takes to arrive) and plays the tail. */
        void end(Minecraft mc) {
            Entity entity = mc.level.getEntity(vehicleId);
            int delay = !firstPerson && entity instanceof VehicleEntity v ? soundDelay(mc, v) : 0;
            long endAt = mc.level.getGameTime() + delay;
            loops.forEach(l -> l.endAt = endAt);
            if (entity instanceof VehicleEntity vehicle) {
                playLayers(mc, vehicle, weapon, firstPerson ? weapon.tail1p : weapon.tail3p, firstPerson);
            }
        }
    }

    private static int soundDelay(Minecraft mc, VehicleEntity vehicle) {
        return Math.round((float) mc.player.getEyePosition().distanceTo(vehicle.position()) / SOUND_SPEED);
    }

    /** Volume of a layer at distance d: Squad's distance crossfade, plus falloff for non-spatial layers. */
    private static float layerVolume(SquadWeaponProfile.Weapon weapon, SquadWeaponProfile.Layer layer, float d, boolean firstPerson) {
        float volume = weapon.volume * layer.gain;
        if (!firstPerson) {
            volume *= layer.crossFade(d);
            if (!layer.spatial && layer.falloff > 0f) volume *= Math.max(0f, 1f - d / layer.falloff);
        }
        return Math.min(volume, 1f);
    }

    /** A burst's stitched fire loop; follows the vehicle and re-applies the distance crossfade each tick. */
    private static final class LoopSound extends AbstractTickableSoundInstance {
        private final VehicleEntity vehicle;
        private final SquadWeaponProfile.Weapon weapon;
        private final SquadWeaponProfile.Layer layer;
        private final boolean firstPerson;
        /** Game time at which the loop stops; -1 while the burst is still going. */
        long endAt = -1;

        LoopSound(VehicleEntity vehicle, SquadWeaponProfile.Weapon weapon, SquadWeaponProfile.Layer layer, boolean firstPerson) {
            super(SoundEvent.createVariableRangeEvent(layer.sound), SOURCE, SoundInstance.createUnseededRandom());
            this.vehicle = vehicle;
            this.weapon = weapon;
            this.layer = layer;
            this.firstPerson = firstPerson;
            this.looping = true;
            this.delay = 0;
            if (layer.spatial && !firstPerson) {
                this.attenuation = SoundInstance.Attenuation.LINEAR;
            } else {
                this.attenuation = SoundInstance.Attenuation.NONE;
                this.relative = true;
            }
            update();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            Minecraft mc = Minecraft.getInstance();
            if (vehicle.isRemoved() || mc.level == null || (endAt >= 0 && mc.level.getGameTime() >= endAt)) {
                stop();
                return;
            }
            update();
        }

        void end() {
            stop();
        }

        private void update() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            float d = (float) mc.player.getEyePosition().distanceTo(vehicle.position());
            volume = Math.max(0f, layerVolume(weapon, layer, d, firstPerson));
            if (!relative) {
                x = vehicle.getX();
                y = vehicle.getY();
                z = vehicle.getZ();
            }
        }
    }

    public static void onShot(int vehicleId, String weaponName, int shooterId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!(mc.level.getEntity(vehicleId) instanceof VehicleEntity vehicle)) return;
        SquadWeaponProfile profile = byVehicle.get(EngineControl.id(vehicle));
        SquadWeaponProfile.Weapon weapon = profile == null ? null : profile.weapons.get(weaponName);
        if (weapon == null) return;

        boolean firstPerson = shooterId == mc.player.getId() && mc.options.getCameraType().isFirstPerson();
        String key = vehicleId + "/" + weaponName;
        Burst burst = BURSTS.get(key);
        if (burst == null || burst.firstPerson != firstPerson) {
            if (burst != null) burst.end(mc);
            burst = new Burst(vehicleId, weapon, firstPerson);
            BURSTS.put(key, burst);
            burst.start(mc, vehicle);
        }
        burst.lastShot = mc.level.getGameTime();
    }

    private static void playLayers(Minecraft mc, VehicleEntity vehicle, SquadWeaponProfile.Weapon weapon,
                                   List<SquadWeaponProfile.Layer> layers, boolean firstPerson) {
        if (layers.isEmpty()) return;
        float d = (float) mc.player.getEyePosition().distanceTo(vehicle.position());
        for (SquadWeaponProfile.Layer layer : layers) {
            float volume = layerVolume(weapon, layer, d, firstPerson);
            if (volume <= 0.001f) continue;
            int delay = Math.round(layer.delay * 20f) + (firstPerson ? 0 : Math.round(d / SOUND_SPEED));
            SoundInstance sound = layer.spatial && !firstPerson
                    ? new SimpleSoundInstance(layer.sound, SOURCE, volume, 1f, RandomSource.create(), false, 0,
                    SoundInstance.Attenuation.LINEAR, vehicle.getX(), vehicle.getY(), vehicle.getZ(), false)
                    : new SimpleSoundInstance(layer.sound, SOURCE, volume, 1f, RandomSource.create(), false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true);
            if (delay > 0) mc.getSoundManager().playDelayed(sound, delay);
            else mc.getSoundManager().play(sound);
        }
    }

    // ── Tick: burst tails and turret traverse ───────────────────────────────────

    @Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
    public static final class Ticker {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != lastLevel) {
                stopAll();
                lastLevel = mc.level;
            }
            if (mc.level == null || mc.player == null || mc.isPaused()) return;
            tickBursts(mc);
            tickTurrets(mc);
        }
    }

    private static void tickBursts(Minecraft mc) {
        long now = mc.level.getGameTime();
        Iterator<Burst> it = BURSTS.values().iterator();
        while (it.hasNext()) {
            Burst burst = it.next();
            if (now - burst.lastShot <= burst.weapon.burstGap) continue;
            burst.end(mc);
            it.remove();
        }
    }

    private static void tickTurrets(Minecraft mc) {
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof VehicleEntity vehicle) || TURRETS.containsKey(vehicle.getId())) continue;
            if (vehicle.distanceToSqr(mc.player) > TURRET_RANGE * TURRET_RANGE) continue;
            SquadWeaponProfile profile = byVehicle.get(EngineControl.id(vehicle));
            if (profile != null && profile.turret != null) {
                TURRETS.put(vehicle.getId(), new TurretAudio(vehicle, profile.turret));
            }
        }
        Iterator<TurretAudio> it = TURRETS.values().iterator();
        while (it.hasNext()) {
            TurretAudio t = it.next();
            if (t.vehicle.isRemoved() || t.vehicle.distanceToSqr(mc.player) > TURRET_RANGE * TURRET_RANGE * 1.2) {
                t.loop.end();
                it.remove();
            } else {
                t.tick(mc);
            }
        }
    }

    /** Squad's Turret_Loop cue: rotation speed drives the loop's volume and pitch. */
    private static final class TurretAudio {
        final VehicleEntity vehicle;
        final SquadWeaponProfile.Turret profile;
        TurretLoop loop;
        float lastYaw, lastPitch, speed;
        boolean inside, moving;

        TurretAudio(VehicleEntity vehicle, SquadWeaponProfile.Turret profile) {
            this.vehicle = vehicle;
            this.profile = profile;
            this.lastYaw = vehicle.getTurretYRot();
            this.lastPitch = vehicle.getTurretXRot();
            this.loop = new TurretLoop(this);
            Minecraft.getInstance().getSoundManager().play(loop);
        }

        void tick(Minecraft mc) {
            float yaw = vehicle.getTurretYRot(), pitch = vehicle.getTurretXRot();
            float dy = Mth.wrapDegrees(yaw - lastYaw), dx = pitch - lastPitch;
            lastYaw = yaw;
            lastPitch = pitch;
            float target = (float) Math.sqrt(dy * dy + dx * dx) * 20f * profile.speedScale;
            // Short smoothing: turret input arrives in steps and the raw speed flickers.
            speed += (target - speed) * 0.5f;

            Player player = mc.player;
            inside = player.getVehicle() == vehicle && mc.options.getCameraType().isFirstPerson();
            boolean gunner = player.getVehicle() == vehicle && vehicle.getSeatIndex(player) == vehicle.getTurretControllerIndex();

            boolean nowMoving = speed >= profile.volumeIn[0];
            SquadWeaponProfile.Turret.Click clickDef = nowMoving ? profile.start : profile.stop;
            if (nowMoving != moving && gunner && clickDef != null) {
                RandomSource random = RandomSource.create();
                mc.getSoundManager().play(new SimpleSoundInstance(clickDef.sound(), SOURCE, clickDef.volume(),
                        Mth.lerp(random.nextFloat(), clickDef.pitchMin(), clickDef.pitchMax()), random, false, 0,
                        SoundInstance.Attenuation.NONE, 0, 0, 0, true));
            }
            moving = nowMoving;
            // Minecraft fixes a sound's relative/attenuation mode when it starts, so switching between
            // the inside (head-relative) and outside (positional) branch needs a new instance. Also
            // covers a loop the sound engine dropped (channel limit, reload).
            if (loop.isStopped() || loop.inside != inside) {
                loop.end();
                loop = new TurretLoop(this);
                mc.getSoundManager().play(loop);
            }
        }

        float volume() {
            float v = SquadWeaponProfile.Turret.modulate(speed, profile.volumeIn, profile.volumeOut);
            return Mth.clamp(v * (inside ? profile.insideVolume : profile.outsideVolume), 0f, 1f);
        }

        float pitch() {
            return Mth.clamp(SquadWeaponProfile.Turret.modulate(speed, profile.pitchIn, profile.pitchOut), 0.5f, 2f);
        }
    }

    private static final class TurretLoop extends AbstractTickableSoundInstance {
        private final TurretAudio audio;
        /** Squad's inside branch: head-relative, for a listener in first person in this vehicle. */
        final boolean inside;
        private boolean ended;

        TurretLoop(TurretAudio audio) {
            super(SoundEvent.createVariableRangeEvent(audio.profile.loop), SOURCE, SoundInstance.createUnseededRandom());
            this.audio = audio;
            this.inside = audio.inside;
            this.looping = true;
            this.delay = 0;
            this.volume = audio.volume();
            this.pitch = audio.pitch();
            if (inside) {
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
            if (ended || audio.vehicle.isRemoved()) {
                stop();
                return;
            }
            if (!inside) follow();
            volume += (audio.volume() - volume) * 0.5f;
            pitch = audio.pitch();
        }

        private void follow() {
            x = audio.vehicle.getX();
            y = audio.vehicle.getY() + 2;
            z = audio.vehicle.getZ();
        }

        void end() {
            ended = true;
            stop();
        }
    }
}
