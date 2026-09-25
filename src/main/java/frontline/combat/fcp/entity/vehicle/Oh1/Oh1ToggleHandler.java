package frontline.combat.fcp.entity.vehicle.Oh1;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModItems;
import frontline.combat.fcp.init.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4d;
import org.joml.Vector4d;

import java.util.Optional;

/**
 * Toggles the OH-1's two stub-wing pods ("toggle1" on the right, "toggle2" on the left)
 * independently, the same way the Ural's TENTY canopy works: right-click the helicopter with
 * the FCP spray while looking at one pod's hitbox. Whichever box the ray actually hits (nearest
 * to the player, in case a look angle somehow lines up with both) is the one toggled; if the ray
 * misses both, the event is left alone so the spray's default camo repaint still works.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class Oh1ToggleHandler {

    private static final double REACH = 5.0;

    // "toggle1" (right pod) hitbox, min/max in SuperbWarfare's vehicle-local space
    // (derived from the geo, same bbox-of-cubes method as the Ural's TENTY box).
    private static final double[] MIN_1 = {0.7447, 0.7955, -0.7083};
    private static final double[] MAX_1 = {1.1634, 1.5173, 0.8769};

    // "toggle2" (left pod) hitbox — the exact mirror of toggle1 across X.
    private static final double[] MIN_2 = {-1.1634, 0.7955, -0.7083};
    private static final double[] MAX_2 = {-0.7447, 1.5173, 0.8769};

    private Oh1ToggleHandler() {}

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != ModItems.SPRAY.get()) return;
        if (!(event.getTarget() instanceof Oh1Entity heli)) return;

        VehicleEntity vehicle = heli;
        Vec3 eye = player.getEyePosition(1f);
        Vec3 end = eye.add(player.getViewVector(1f).scale(REACH));
        Matrix4d transform = VehicleVecUtils.INSTANCE.getVehicleYOffsetTransform(vehicle, 1f);
        double root = vehicle.getRotateOffsetHeight();

        AABB box1 = toWorldBox(MIN_1, MAX_1, transform, root);
        AABB box2 = toWorldBox(MIN_2, MAX_2, transform, root);

        Optional<Vec3> hit1 = box1.clip(eye, end);
        Optional<Vec3> hit2 = box2.clip(eye, end);

        Runnable toggle;
        if (hit1.isPresent() && (hit2.isEmpty() || hit1.get().distanceToSqr(eye) <= hit2.get().distanceToSqr(eye))) {
            toggle = heli::toggleToggle1;
        } else if (hit2.isPresent()) {
            toggle = heli::toggleToggle2;
        } else {
            return; // neither box hit: leave the event for the vehicle's own spray handler
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!player.level().isClientSide()) {
            toggle.run();
            player.level().playSound(null, heli.blockPosition(),
                    ModSounds.SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.swing(event.getHand());
    }

    private static AABB toWorldBox(double[] min, double[] max, Matrix4d transform, double root) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? min[0] : max[0];
            double y = (i & 2) == 0 ? min[1] : max[1];
            double z = (i & 4) == 0 ? min[2] : max[2];
            Vector4d w = transform.transform(new Vector4d(x, y - root, z, 1.0));
            minX = Math.min(minX, w.x); maxX = Math.max(maxX, w.x);
            minY = Math.min(minY, w.y); maxY = Math.max(maxY, w.y);
            minZ = Math.min(minZ, w.z); maxZ = Math.max(maxZ, w.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
