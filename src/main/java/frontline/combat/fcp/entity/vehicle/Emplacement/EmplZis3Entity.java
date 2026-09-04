package frontline.combat.fcp.entity.vehicle.Emplacement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EmplZis3Entity extends ClampedTurretEntity {

    /** Loaded shell by shell: each right-click consumes the round in hand, nothing else. */
    @Override
    protected boolean usesInventoryReload() {
        return false;
    }
    public EmplZis3Entity(EntityType<? extends com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity> type, Level world) {super(type, world);}

    @Override protected double[] legOffset() { return new double[]{1.5, 0.1, -1.8}; }

    @Override protected double[] bodyBox() { return new double[]{1.0, 0.8, 0.5, 0.0, 0.9, 0.75}; }
}