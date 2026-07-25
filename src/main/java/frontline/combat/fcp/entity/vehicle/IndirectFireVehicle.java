package frontline.combat.fcp.entity.vehicle;

import frontline.combat.fcp.firecontrol.FireControlComputation;
import frontline.combat.fcp.firecontrol.FireControlStatus;
import frontline.combat.fcp.firecontrol.TrajectoryMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public interface IndirectFireVehicle {
    boolean isFireControlActive();

    BlockPos getFireControlTarget();

    int getFireControlRadius();

    TrajectoryMode getFireControlTrajectory();

    FireControlStatus getFireControlStatus();

    FireControlComputation getFireControlComputation();

    boolean applyFireControl(BlockPos target, int radius, TrajectoryMode mode, Entity actor);

    void clearFireControl(Entity actor);
}
