package frontline.combat.fcp.entity.vehicle.Mi17;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Toggles each of MI17DoorGuns's independent cosmetic toggle bones the same way the
 * OH-1's stub-wing pods work: right-click the helicopter with the FCP spray while looking at one
 * toggle's hitbox. Whichever box the ray actually hits nearest to the player is the one toggled;
 * if the ray misses all of them, the event is left alone so the spray's default camo repaint
 * still works. Generalised to N independent toggles (OH-1 only ever needed two).
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class MI17DoorGunsToggleHandler {

    private static final double REACH = 5.0;

    private static final double[] MIN_TOGGLE6 = {-1.717, 0.135, 1.2675};
    private static final double[] MAX_TOGGLE6 = {1.7138, 0.9858, 1.8678};
    private static final double[] MIN_TOGGLE4 = {-0.5917, 3.5214, -2.8323};
    private static final double[] MAX_TOGGLE4 = {0.5754, 3.8131, -2.3835};
    private static final double[] MIN_TOGGLE5 = {-1.3499, 2.4833, 0.4569};
    private static final double[] MAX_TOGGLE5 = {-0.7963, 2.7826, 2.4368};
    private static final double[] MIN_TOGGLE2 = {-1.4139, 2.9696, -1.1291};
    private static final double[] MAX_TOGGLE2 = {1.4106, 3.7178, 0.2773};
    private static final double[] MIN_TOGGLE1 = {-1.2384, 1.0169, 2.8223};
    private static final double[] MAX_TOGGLE1 = {1.2352, 1.6741, 3.9245};
    private static final double[] MIN_TOGGLE3 = {-1.2942, 2.431, -3.0443};
    private static final double[] MAX_TOGGLE3 = {1.2909, 2.7452, -1.9072};
    private static final double[] MIN_TOGGLE = {-0.5151, 3.0335, 2.1715};
    private static final double[] MAX_TOGGLE = {0.513, 3.3328, 2.3209};

    private MI17DoorGunsToggleHandler() {}

    private record Candidate(AABB box, Runnable toggle) {}

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != ModItems.SPRAY.get()) return;
        if (!(event.getTarget() instanceof MI17DoorGunsEntity heli)) return;

        VehicleEntity vehicle = heli;
        Vec3 eye = player.getEyePosition(1f);
        Vec3 end = eye.add(player.getViewVector(1f).scale(REACH));
        Matrix4d transform = VehicleVecUtils.INSTANCE.getVehicleYOffsetTransform(vehicle, 1f);
        double root = vehicle.getRotateOffsetHeight();

        List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate(toWorldBox(MIN_TOGGLE6, MAX_TOGGLE6, transform, root), heli::toggleToggle6));
        candidates.add(new Candidate(toWorldBox(MIN_TOGGLE4, MAX_TOGGLE4, transform, root), heli::toggleToggle4));
        candidates.add(new Candidate(toWorldBox(MIN_TOGGLE5, MAX_TOGGLE5, transform, root), heli::toggleToggle5));
        candidates.add(new Candidate(toWorldBox(MIN_TOGGLE2, MAX_TOGGLE2, transform, root), heli::toggleToggle2));
        candidates.add(new Candidate(toWorldBox(MIN_TOGGLE1, MAX_TOGGLE1, transform, root), heli::toggleToggle1));
        candidates.add(new Candidate(toWorldBox(MIN_TOGGLE3, MAX_TOGGLE3, transform, root), heli::toggleToggle3));
        candidates.add(new Candidate(toWorldBox(MIN_TOGGLE, MAX_TOGGLE, transform, root), heli::toggleToggle));

        Runnable nearest = null;
        double nearestDistSqr = Double.MAX_VALUE;
        for (Candidate c : candidates) {
            Optional<Vec3> hit = c.box().clip(eye, end);
            if (hit.isPresent()) {
                double d = hit.get().distanceToSqr(eye);
                if (d < nearestDistSqr) {
                    nearestDistSqr = d;
                    nearest = c.toggle();
                }
            }
        }

        if (nearest == null) return; // no box hit: leave the event for the vehicle's own spray handler

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!player.level().isClientSide()) {
            nearest.run();
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
