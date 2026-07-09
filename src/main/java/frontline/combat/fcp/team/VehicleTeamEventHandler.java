package frontline.combat.fcp.team;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import frontline.combat.fcp.FCP;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class VehicleTeamEventHandler {

    private static final Map<UUID, Long> LAST_MESSAGE = new HashMap<>();
    private static final double TARGET_REACH = 12.0;

    private VehicleTeamEventHandler() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        TeamLockConfig.load();
    }



    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        handleInteract(event, event.getTarget(), event.getEntity(), event.getItemStack());
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handleInteract(event, event.getTarget(), event.getEntity(), event.getItemStack());
    }

    private static void handleInteract(PlayerInteractEvent event, Entity target, Player player, ItemStack stack) {
        if (!(target instanceof VehicleEntity vehicle)) return;
        if (player.level().isClientSide()) return;
        if (player instanceof FakePlayer) return;

        TeamLockConfig cfg = TeamLockConfig.get();
        if (!cfg.enforce) return;
        if (!cfg.applyToNonFcpVehicles && !VehicleTeamLock.isFcpVehicle(vehicle)) return;
        if (cfg.opBypass && (player.hasPermissions(2) || player.isCreative())) return;
        if (isBypassItem(cfg, stack)) return;

        if (!isTeamBlocked(cfg, vehicle, player)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        notify(player, denyMessage(vehicle), false);
    }


    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (!event.isMounting()) return;

        if (!(event.getEntityBeingMounted() instanceof VehicleEntity vehicle)) return;
        if (!(event.getEntityMounting() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (player instanceof FakePlayer) return;

        TeamLockConfig cfg = TeamLockConfig.get();
        if (!cfg.enforce) return;
        if (!cfg.applyToNonFcpVehicles && !VehicleTeamLock.isFcpVehicle(vehicle)) return;
        if (cfg.opBypass && (player.hasPermissions(2) || player.isCreative())) return;

        String vehicleTeam = VehicleTeamLock.getTeam(vehicle);

        if (vehicleTeam == null) {
            if (cfg.autoClaimOnEnter && vehicle.getPassengers().isEmpty()) {
                String playerTeam = VehicleTeamLock.teamNameOf(player);
                if (playerTeam != null) {
                    VehicleTeamLock.setTeam(vehicle, playerTeam);
                    notify(player, Component.literal("Vehicle claimed for team ")
                            .append(Component.literal(playerTeam).withStyle(ChatFormatting.YELLOW))
                            .append("."), true);
                }
                return;
            }
            if (cfg.blockUnclaimed) {
                event.setCanceled(true);
                notify(player, Component.literal("This vehicle is locked. Ask an admin to assign it to your team.")
                        .withStyle(ChatFormatting.RED), false);
            }
            return;
        }

        if (!vehicleTeam.equals(VehicleTeamLock.teamNameOf(player))) {
            event.setCanceled(true);
            notify(player, denyMessage(vehicle), false);
        }
    }


    private static boolean isTeamBlocked(TeamLockConfig cfg, VehicleEntity vehicle, Player player) {
        String vehicleTeam = VehicleTeamLock.getTeam(vehicle);
        if (vehicleTeam == null) {
            return cfg.blockUnclaimed;
        }
        return !vehicleTeam.equals(VehicleTeamLock.teamNameOf(player));
    }

    private static boolean isBypassItem(TeamLockConfig cfg, ItemStack stack) {
        if (cfg.interactionBypassItems.isEmpty() || stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && cfg.interactionBypassItems.contains(id.toString());
    }

    private static Component denyMessage(VehicleEntity vehicle) {
        String team = VehicleTeamLock.getTeam(vehicle);
        if (team == null) {
            return Component.literal("This vehicle is locked.").withStyle(ChatFormatting.RED);
        }
        return Component.literal("This vehicle belongs to team ")
                .append(Component.literal(team).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" - you can't use it.").withStyle(ChatFormatting.RED))
                .withStyle(ChatFormatting.RED);
    }

    private static void notify(Player player, Component msg, boolean actionBar) {
        long now = player.level().getGameTime();
        Long last = LAST_MESSAGE.get(player.getUUID());
        int cd = TeamLockConfig.get().messageCooldownTicks;
        if (last != null && now - last < cd) return;
        LAST_MESSAGE.put(player.getUUID(), now);
        if (player instanceof ServerPlayer sp) {
            sp.displayClientMessage(msg, actionBar);
        }
    }


    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("fcpvehicle")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("team")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("team", TeamArgument.team())
                                                .executes(ctx -> {
                                                    PlayerTeam team = TeamArgument.getTeam(ctx, "team");
                                                    return setTeam(ctx.getSource().getPlayerOrException(), team.getName());
                                                })))
                                .then(Commands.literal("clear")
                                        .executes(ctx -> clearTeam(ctx.getSource().getPlayerOrException())))
                                .then(Commands.literal("query")
                                        .executes(ctx -> queryTeam(ctx.getSource().getPlayerOrException())))));
    }

    private static VehicleEntity targetVehicle(ServerPlayer player) {
        if (player.getVehicle() instanceof VehicleEntity ridden) return ridden;

        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(TARGET_REACH));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(TARGET_REACH)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, eye, end, searchBox,
                (Entity e) -> e instanceof VehicleEntity,
                TARGET_REACH * TARGET_REACH);
        if (hit != null && hit.getEntity() instanceof VehicleEntity looked) return looked;
        return null;
    }

    private static int setTeam(ServerPlayer player, String teamName) throws CommandSyntaxException {
        VehicleEntity v = targetVehicle(player);
        if (v == null) {
            player.sendSystemMessage(noTarget());
            return 0;
        }
        VehicleTeamLock.setTeam(v, teamName);
        player.sendSystemMessage(Component.literal("[FCP] Vehicle assigned to team ")
                .append(Component.literal(teamName).withStyle(ChatFormatting.YELLOW))
                .append("."));
        return 1;
    }

    private static int clearTeam(ServerPlayer player) throws CommandSyntaxException {
        VehicleEntity v = targetVehicle(player);
        if (v == null) {
            player.sendSystemMessage(noTarget());
            return 0;
        }
        VehicleTeamLock.clearTeam(v);
        player.sendSystemMessage(Component.literal("[FCP] Vehicle team cleared (now neutral)."));
        return 1;
    }

    private static int queryTeam(ServerPlayer player) throws CommandSyntaxException {
        VehicleEntity v = targetVehicle(player);
        if (v == null) {
            player.sendSystemMessage(noTarget());
            return 0;
        }
        String t = VehicleTeamLock.getTeam(v);
        player.sendSystemMessage(t == null
                ? Component.literal("[FCP] This vehicle is neutral (no team).")
                : Component.literal("[FCP] This vehicle belongs to team ")
                .append(Component.literal(t).withStyle(ChatFormatting.YELLOW))
                .append("."));
        return 1;
    }

    private static Component noTarget() {
        return Component.literal("[FCP] No vehicle found - look at a vehicle (within "
                        + (int) TARGET_REACH + " blocks) or sit in one, then run the command.")
                .withStyle(ChatFormatting.RED);
    }
}