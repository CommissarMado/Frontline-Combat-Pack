package frontline.combat.fcp.entity.vehicle.Stugna;

import com.atsuishio.superbwarfare.entity.projectile.WireGuideMissileEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.FCPConfig;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.StugnaRemoteStatePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server side of Stugna-P remote control.
 *
 * <p>A session links a player, the control panel they used and the launcher they operate. The
 * player stays at the panel; the client moves only their camera to the launcher's sight
 * (StugnaRemoteClient). Because nobody rides the launcher, SBW's own fire packet and wire-guidance
 * (both of which require the shooter to be a passenger) don't apply, so this class fires the
 * launcher on request and steers its missiles along the launcher's barrel line - a beam-riding
 * missile, like the real Stugna-P.
 *
 * <p>The session ends - and the camera snaps back - if the operator leaves the panel, takes damage,
 * mounts something, dies, logs out or changes dimension, or if the panel or launcher is destroyed.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class StugnaRemote {

    private record Session(ResourceKey<Level> dimension, int controllerId, int launcherId) {
    }

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    /** How far the operator may drift from the panel before the link drops. */
    private static final double MAX_OPERATOR_DISTANCE = 4.0;
    /** Missiles are only steered while within this distance of their launcher. */
    private static final double GUIDANCE_RANGE = 512.0;
    /** How far ahead on the beam the missile aims, and how hard it turns towards it per tick. */
    private static final double BEAM_LEAD = 6.0;
    private static final double TURN_RATE = 0.35;
    private static final double AIM_DISTANCE = 512.0;

    private StugnaRemote() {
    }

    // ---------------------------------------------------------------------------------------------
    // Session control
    // ---------------------------------------------------------------------------------------------

    /** Control-panel right-click: start operating the nearest free launcher, or stop if already operating. */
    public static void toggle(ServerPlayer player, StugnaControllerEntity controller) {
        if (SESSIONS.containsKey(player.getUUID())) {
            stop(player, null);
            return;
        }
        int range = FCPConfig.stugnaLinkRange();
        ServerLevel level = player.serverLevel();
        StugnaLauncherEntity launcher = level.getEntitiesOfClass(StugnaLauncherEntity.class,
                        controller.getBoundingBox().inflate(range),
                        l -> l.isAlive() && !l.isWreck() && !l.hasOperator()
                                && l.distanceTo(controller) <= range)
                .stream()
                .min(Comparator.comparingDouble(l -> l.distanceToSqr(controller)))
                .orElse(null);
        if (launcher == null) {
            player.displayClientMessage(Component.literal("No free Stugna-P launcher within " + range + " blocks"), true);
            return;
        }
        if (player.isPassenger()) {
            player.stopRiding();
        }
        launcher.setOperator(player);
        // The sight starts where the operator is looking; from here on it moves independently.
        launcher.setAim(player.getYRot(), player.getXRot());
        SESSIONS.put(player.getUUID(), new Session(level.dimension(), controller.getId(), launcher.getId()));
        FCPNetwork.FCP_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new StugnaRemoteStatePacket(launcher.getId()));
        player.displayClientMessage(Component.literal("Linked to Stugna-P - LMB fire, RMB zoom, SHIFT disconnect"), true);
    }

    /** Ends the player's session (if any) and returns their camera. */
    public static void stop(ServerPlayer player, @Nullable String reason) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session == null) {
            return;
        }
        ServerLevel level = player.server.getLevel(session.dimension());
        if (level != null && level.getEntity(session.launcherId()) instanceof StugnaLauncherEntity launcher
                && launcher.isOperator(player)) {
            launcher.setOperator(null);
        }
        FCPNetwork.FCP_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new StugnaRemoteStatePacket(-1));
        if (reason != null) {
            player.displayClientMessage(Component.literal(reason), true);
        }
    }

    public static boolean isOperating(@Nullable Player player, StugnaLauncherEntity launcher) {
        if (player == null) {
            return false;
        }
        Session session = SESSIONS.get(player.getUUID());
        return session != null && session.launcherId() == launcher.getId();
    }

    @Nullable
    private static StugnaLauncherEntity launcherOf(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || !player.level().dimension().equals(session.dimension())) {
            return null;
        }
        return player.level().getEntity(session.launcherId()) instanceof StugnaLauncherEntity launcher ? launcher : null;
    }

    // ---------------------------------------------------------------------------------------------
    // Remote fire (StugnaRemoteActionPacket)
    // ---------------------------------------------------------------------------------------------

    public static void fire(ServerPlayer player) {
        StugnaLauncherEntity launcher = launcherOf(player);
        if (launcher == null || launcher.isWreck() || !launcher.isOperator(player)) {
            return;
        }
        if (!launcher.canShoot(player)) {
            player.displayClientMessage(Component.literal("No missile loaded"), true);
            return;
        }
        // Aim at whatever lies under the crosshair as seen from the launcher's sight.
        Vec3 sight = VehicleVecUtils.getCameraPos(launcher, player, 1f);
        Vec3 target = sight.add(launcher.getAimVector().scale(AIM_DISTANCE));
        launcher.vehicleShoot(player, (UUID) null, target);
    }

    /** StugnaAimPacket: the operator moved their sight. */
    public static void setAim(ServerPlayer player, float yaw, float pitch) {
        StugnaLauncherEntity launcher = launcherOf(player);
        if (launcher != null && launcher.isOperator(player)) {
            launcher.setAim(yaw, pitch);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Per-tick validation and missile guidance
    // ---------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || SESSIONS.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<UUID, Session>> it = SESSIONS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Session> entry = it.next();
            Session session = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            ServerLevel level = server.getLevel(session.dimension());
            if (player == null || level == null) {
                it.remove();
                if (level != null && level.getEntity(session.launcherId()) instanceof StugnaLauncherEntity launcher) {
                    launcher.setOperator(null);
                }
                continue;
            }
            String reason = validate(player, level, session);
            if (reason != null) {
                stop(player, reason);
                continue;
            }
            if (level.getEntity(session.launcherId()) instanceof StugnaLauncherEntity launcher) {
                steerMissiles(level, launcher, player);
            }
        }
    }

    /** Returns why the session must end, or null if it is still valid. */
    @Nullable
    private static String validate(ServerPlayer player, ServerLevel level, Session session) {
        if (!player.isAlive() || player.level() != level) {
            return "Stugna-P disconnected";
        }
        Entity controller = level.getEntity(session.controllerId());
        if (!(controller instanceof StugnaControllerEntity) || !controller.isAlive()) {
            return "Control panel lost";
        }
        Entity launcher = level.getEntity(session.launcherId());
        if (!(launcher instanceof StugnaLauncherEntity stugna) || !stugna.isAlive() || stugna.isWreck()) {
            return "Launcher lost";
        }
        if (player.isPassenger() || player.distanceTo(controller) > MAX_OPERATOR_DISTANCE) {
            return "Stugna-P disconnected";
        }
        if (!stugna.isOperator(player)) {
            stugna.setOperator(player);
        }
        return null;
    }

    /**
     * Beam riding: keeps each missile fired by this launcher on the line from its shoot position
     * along the barrel, aiming a little ahead of the missile on that line.
     */
    private static void steerMissiles(ServerLevel level, StugnaLauncherEntity launcher, ServerPlayer operator) {
        UUID launcherId = launcher.getUUID();
        AABB area = launcher.getBoundingBox().inflate(GUIDANCE_RANGE);
        // SBW only stamps a wire-guided missile with its launcher when the shooter RIDES the vehicle
        // (shooter.getVehicle()), which the remote operator never does - so pick our missiles out by
        // owner instead and stamp them ourselves, so a later operator's missiles are never confused.
        for (WireGuideMissileEntity missile : level.getEntitiesOfClass(WireGuideMissileEntity.class, area,
                m -> operator.equals(m.getOwner())
                        && (m.getLauncherVehicleUUID() == null || launcherId.equals(m.getLauncherVehicleUUID())))) {
            if (missile.getLauncherVehicleUUID() == null) {
                missile.setLauncherVehicleUUID(launcherId);
            }
            Vec3 origin = launcher.getShootPosForHud(operator, 1f);
            Vec3 beam = launcher.getBarrelVector(1f).normalize();
            Vec3 pos = missile.position();
            double along = pos.subtract(origin).dot(beam);
            Vec3 aimPoint = origin.add(beam.scale(Math.max(0, along) + BEAM_LEAD));
            Vec3 velocity = missile.getDeltaMovement();
            double speed = velocity.length();
            if (speed < 1.0E-3) {
                continue;
            }
            Vec3 desired = aimPoint.subtract(pos).normalize().scale(speed);
            Vec3 steered = velocity.add(desired.subtract(velocity).scale(TURN_RATE)).normalize().scale(speed);
            missile.setDeltaMovement(steered);
            // Point the nose along the new path: the missile's own flight code pushes it along its
            // facing, which would otherwise pull it back onto its launch heading.
            double horizontal = Math.sqrt(steered.x * steered.x + steered.z * steered.z);
            missile.setYRot((float) Math.toDegrees(Math.atan2(-steered.x, steered.z)));
            missile.setXRot((float) Math.toDegrees(-Math.atan2(steered.y, horizontal)));
            missile.hurtMarked = true; // sync the new velocity to clients now
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Things that drop the link
    // ---------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && SESSIONS.containsKey(player.getUUID())) {
            stop(player, "Stugna-P disconnected - you were hit");
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            stop(player, null);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            stop(player, null);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            stop(player, null);
        }
    }
}
