package frontline.combat.fcp.entity.vehicle.Emplacement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EmplMg3Entity extends ClampedTurretEntity {
    // Shares the M2 texture atlas (same tripod/mount, only the gun bone differs in the geo).
    public EmplMg3Entity(EntityType<EmplMg3Entity> type, Level world) {super(type, world);}
    @Override public com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier getDamageModifier() {return super.getDamageModifier().custom((entity, s, dmg) -> getSourceAngle(s, 0.4f) * dmg);}

    @Override protected double[] legOffset() { return new double[]{0.5, 0.1, -0.7}; }
    @Override protected boolean needsManualReload() { return false; }
}
