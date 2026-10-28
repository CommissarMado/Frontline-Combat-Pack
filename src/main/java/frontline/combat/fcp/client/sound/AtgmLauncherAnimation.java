package frontline.combat.fcp.client.sound;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.entity.vehicle.AtgmLauncherVehicle;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * ATGM launchers that deploy when the gunner selects the ATGM (AtgmLauncherVehicle): the Puma's Spike covers
 * swing open, the Bradley's TOW launcher swings up. Deploying takes the length of Squad's Bradley TOW "up" sound,
 * which plays with it; switching to another weapon, or reloading the ATGM, stows it over the "down" sound.
 * Purely client-side, driven by the gunner's selected weapon (synced by SBW), so every player sees and hears it.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class AtgmLauncherAnimation {

    /** Lengths of the Squad cues (bradley_TOW_up/down_ext_Cue), in ticks. */
    private static final float OPEN_TICKS = 6.30f * 20f, CLOSE_TICKS = 5.66f * 20f;
    /** Launchers are animated within this distance at most; sounds play within it. */
    private static final double RANGE = 96.0;

    private static final SoundEvent UP_EXT = event("squad.bradley.tow.up_ext");
    private static final SoundEvent UP_INT = event("squad.bradley.tow.up_int");
    private static final SoundEvent DOWN_EXT = event("squad.bradley.tow.down_ext");
    private static final SoundEvent DOWN_INT = event("squad.bradley.tow.down_int");

    private static final Map<VehicleEntity, State> STATES = new WeakHashMap<>();

    private static final class State {
        boolean open;
        /** 0 = closed, 1 = fully open. */
        float progress, progressO;
    }

    private AtgmLauncherAnimation() {
    }

    /** How far deployed the launcher is (0..1), smoothed over the partial tick. */
    public static float openness(VehicleEntity vehicle, float partialTick) {
        State s = STATES.get(vehicle);
        if (s == null) return atgmSelected(vehicle) ? 1f : 0f;
        float t = Mth.lerp(partialTick, s.progressO, s.progress);
        // Ease in and out like a powered drive.
        return t * t * (3f - 2f * t);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof VehicleEntity vehicle) || !(e instanceof AtgmLauncherVehicle launcher) || !launcher.hasAtgmLauncher()) continue;
            if (vehicle.distanceToSqr(mc.player) > RANGE * RANGE) continue;
            boolean wantOpen = atgmSelected(vehicle);
            State s = STATES.get(vehicle);
            if (s == null) {
                // First seen: take the current state as it is, no sound.
                s = new State();
                s.open = wantOpen;
                s.progress = s.progressO = wantOpen ? 1f : 0f;
                STATES.put(vehicle, s);
                continue;
            }
            if (wantOpen != s.open) {
                s.open = wantOpen;
                play(mc, vehicle, wantOpen);
            }
            s.progressO = s.progress;
            s.progress = s.open
                    ? Math.min(1f, s.progress + 1f / OPEN_TICKS)
                    : Math.max(0f, s.progress - 1f / CLOSE_TICKS);
        }
    }

    /** Open while the gunner has the ATGM selected, except while it reloads. */
    private static boolean atgmSelected(VehicleEntity vehicle) {
        if (!(vehicle instanceof AtgmLauncherVehicle launcher)) return false;
        return launcher.atgmWeapon().equals(CannonRecoilTransforms.gunnerGunName(vehicle)) && !launcher.atgmReloading();
    }

    private static void play(Minecraft mc, VehicleEntity vehicle, boolean open) {
        boolean inside = mc.player.getVehicle() == vehicle && mc.options.getCameraType().isFirstPerson();
        SoundInstance sound = inside
                ? new SimpleSoundInstance((open ? UP_INT : DOWN_INT).getLocation(), SoundSource.NEUTRAL, 1f, 1f,
                RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true)
                : new SimpleSoundInstance(open ? UP_EXT : DOWN_EXT, SoundSource.NEUTRAL, 1f, 1f,
                RandomSource.create(), vehicle.getX(), vehicle.getY() + 2, vehicle.getZ());
        mc.getSoundManager().play(sound);
    }

    private static SoundEvent event(String name) {
        return SoundEvent.createVariableRangeEvent(new ResourceLocation(FCP.MODID, name));
    }
}
