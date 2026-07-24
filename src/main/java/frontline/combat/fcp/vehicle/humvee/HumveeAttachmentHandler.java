package frontline.combat.fcp.vehicle.humvee;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModItems;
import frontline.combat.fcp.init.ModSounds;
import net.minecraft.sounds.SoundSource;
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
 * consumed. If the click does NOT land on an attachment hitbox the event is left alone, so
 * the spray's default use on a vehicle (cycling the camo) still works everywhere else.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class HumveeAttachmentHandler {

    // A click counts for a category if it lands within this distance of its hitbox centre.
    private static final double PICK_RADIUS = 0.8;

    private HumveeAttachmentHandler() {}

    @SubscribeEvent
    public static void onInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        Entity target = event.getTarget();
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty() || stack.getItem() != ModItems.SPRAY.get()) return;
        if (!(target instanceof HumveeVehicle humvee)) return;

        List<HumveeAttachments.Category> categories = HumveeAttachments.categories(humvee.humveeName());
        if (categories.isEmpty()) return;

        // event.getLocalPos() is the clicked point relative to the entity (world-aligned).
        // Rotate it into the vehicle's local frame before comparing to the hitbox centres.
        double yawRad = Math.toRadians(target.getYRot());
        Vec3 local = event.getLocalPos().yRot((float) yawRad);

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

        // No attachment under the click: leave the event alone so the vehicle's own spray
        // handler (camo repaint) runs as normal.
        if (best == null) return;

        // Claim the interaction so it doesn't fall through to repaint / mounting.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (!player.level().isClientSide()) {
            humvee.cycleAttachment(best.name, best.variantCount);
            // Same audio feedback as changing the camo.
            player.level().playSound(null, target.blockPosition(),
                    ModSounds.SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.swing(event.getHand());
    }
}
