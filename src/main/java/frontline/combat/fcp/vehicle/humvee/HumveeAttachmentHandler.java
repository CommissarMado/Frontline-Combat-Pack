package frontline.combat.fcp.vehicle.humvee;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModItems;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Right-clicking a Humvee's attachment with the FCP spray cycles that attachment to its
 * next variant (including the empty "removed" variants). The spray is a tool and is never
 * consumed. Interaction is chosen by whichever attachment hitbox the click landed nearest.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class HumveeAttachmentHandler {

    // A click counts for a category if it lands within this distance of its hitbox centre.
    private static final double PICK_RADIUS = 0.8;

    private HumveeAttachmentHandler() {}

    @SubscribeEvent
    public static void onInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handle(event, event.getTarget(), event.getEntity(), event.getItemStack(), event.getLocalPos());
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        // No precise local position here; fall back to the eye->target vector.
        Entity t = event.getTarget();
        Vec3 approx = event.getEntity().getEyePosition().subtract(t.position());
        handle(event, t, event.getEntity(), event.getItemStack(), approx);
    }

    private static void handle(PlayerInteractEvent event, Entity target, Player player,
                               ItemStack stack, Vec3 localHit) {
        if (stack.isEmpty() || stack.getItem() != ModItems.SPRAY.get()) return;
        if (!(target instanceof HumveeVehicle humvee)) return;

        List<HumveeAttachments.Category> categories = HumveeAttachments.categories(humvee.humveeName());
        if (categories.isEmpty()) return;

        // Always claim the interaction so the spray doesn't fall through to mounting etc.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (player.level().isClientSide()) return;

        // localHit is world-relative (hit - entity pos). Rotate into the vehicle's local
        // frame (undoing the body yaw the model is drawn with) before comparing.
        double yawRad = Math.toRadians(target.getYRot());
        Vec3 local = localHit.yRot((float) yawRad);

        HumveeAttachments.Category best = null;
        double bestDist = PICK_RADIUS * PICK_RADIUS;
        for (HumveeAttachments.Category c : categories) {
            double dx = local.x - c.hitbox[0];
            double dy = local.y - c.hitbox[1];
            double dz = local.z - c.hitbox[2];
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 <= bestDist) {
                bestDist = d2;
                best = c;
            }
        }
        if (best != null) {
            humvee.cycleAttachment(best.name, best.variantCount);
        }
    }
}
