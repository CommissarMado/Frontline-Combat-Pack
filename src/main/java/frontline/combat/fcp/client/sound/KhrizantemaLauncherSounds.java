package frontline.combat.fcp.client.sound;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Khrizantema.KhrizantemaEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The Khrizantema's launcher drive: Squad's Bradley TOW "up" cue plays as the arm starts rising out
 * of the hull, the "down" cue as it starts folding back in (the arm's animation is timed to these cues, see
 * KhrizantemaEntity). Client-side, from the synced arm progress.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class KhrizantemaLauncherSounds {

    private static final double RANGE = 96.0;

    private static final SoundEvent UP_EXT = event("squad.bradley.tow.up_ext");
    private static final SoundEvent UP_INT = event("squad.bradley.tow.up_int");
    private static final SoundEvent DOWN_EXT = event("squad.bradley.tow.down_ext");
    private static final SoundEvent DOWN_INT = event("squad.bradley.tow.down_int");

    /** Last seen arm progress and which way the launcher was last heard moving (+1 up, -1 down, 0 none). */
    private static final class State {
        float last;
        int heard;
    }

    private static final Map<KhrizantemaEntity, State> STATES = new WeakHashMap<>();

    private KhrizantemaLauncherSounds() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof KhrizantemaEntity vehicle)) continue;
            if (vehicle.distanceToSqr(mc.player) > RANGE * RANGE) continue;
            float now = vehicle.arm();
            State s = STATES.get(vehicle);
            if (s == null) {
                // First seen: take it as it is, no sound.
                s = new State();
                s.last = now;
                STATES.put(vehicle, s);
                continue;
            }
            int dir = Float.compare(now, s.last);
            boolean inLauncher = now > 0 && now < KhrizantemaEntity.LAUNCHER
                    || s.last > 0 && s.last < KhrizantemaEntity.LAUNCHER;
            if (dir != 0 && inLauncher && dir != s.heard) {
                s.heard = dir;
                play(mc, vehicle, dir > 0);
            } else if (!inLauncher) {
                s.heard = 0;
            }
            s.last = now;
        }
    }

    private static void play(Minecraft mc, KhrizantemaEntity vehicle, boolean up) {
        boolean inside = mc.player.getVehicle() == vehicle && mc.options.getCameraType().isFirstPerson();
        SoundInstance sound = inside
                ? new SimpleSoundInstance((up ? UP_INT : DOWN_INT).getLocation(), SoundSource.NEUTRAL, 1f, 1f,
                RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true)
                : new SimpleSoundInstance(up ? UP_EXT : DOWN_EXT, SoundSource.NEUTRAL, 1f, 1f,
                RandomSource.create(), vehicle.getX(), vehicle.getY() + 2, vehicle.getZ());
        mc.getSoundManager().play(sound);
    }

    private static SoundEvent event(String name) {
        return SoundEvent.createVariableRangeEvent(new ResourceLocation(FCP.MODID, name));
    }
}
