package frontline.combat.fcp.entity.vehicle.Stugna;

import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Stugna-P control panel. Right-click it with an empty hand to take remote control of the nearest
 * free {@link StugnaLauncherEntity} within the configured link range; the operator stays standing
 * here and only their camera moves to the launcher (see {@link StugnaRemote}). With an item in hand
 * it behaves like any other SBW vehicle (crowbar pickup, repair, spray...).
 *
 * <p>Like the stationary launchers it is a fixed installation: no seats are ever used, and only the
 * entity's own bounding box acts as its hitbox.
 */
public class StugnaControllerEntity extends CamoVehicleBase {

    private Vec3 lockPos;

    public StugnaControllerEntity(EntityType<StugnaControllerEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return super.interact(player, hand);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            StugnaRemote.toggle(serverPlayer, this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    /** Never ridden - it only hands control over to a launcher. */
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    // Fixed installation, same as the emplacements: nothing can shove it.
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.lockPos == null) {
            this.lockPos = this.position();
        } else if (this.getX() != this.lockPos.x || this.getZ() != this.lockPos.z) {
            this.setPos(this.lockPos.x, this.getY(), this.lockPos.z);
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
    }
}
