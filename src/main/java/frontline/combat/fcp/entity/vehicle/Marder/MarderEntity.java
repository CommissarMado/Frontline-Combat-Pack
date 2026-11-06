package frontline.combat.fcp.entity.vehicle.Marder;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.entity.vehicle.LauncherTube;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;

/**
 * Marder 1A3. The gunner has the 20 mm, the coax MG3 and the Milan, whose launcher ("barrel2") elevates with the gun
 * about its own pivot. As on the Puma, that pivot is exposed to the vehicle JSON as the transform "AtgmBarrel" - built
 * like SBW's barrel transform (turret -> pivot -> gun pitch) - so missiles leave the launcher tube.
 */
public class MarderEntity extends CamoVehicleBase {

    public static final String ATGM_WEAPON = "ATGM";
    public static final String ATGM_BARREL = "AtgmBarrel";

    /** The "barrel2" bone's pivot relative to the turret pivot, in blocks (config convention). */
    private static final double[] ATGM_PIVOT = {-0.6666, 0.2105, 0.1880};

    /** The spent launch tube is off the launcher (LauncherTube). */
    private static final EntityDataAccessor<Boolean> TUBE_OFF =
            SynchedEntityData.defineId(MarderEntity.class, EntityDataSerializers.BOOLEAN);

    public int INVENTORY_SIZE = 18;

    public MarderEntity(EntityType<MarderEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TUBE_OFF, false);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level().isClientSide) this.entityData.set(TUBE_OFF, LauncherTube.off(this, ATGM_WEAPON));
    }

    public boolean launcherTubeOff() {
        return this.entityData.get(TUBE_OFF);
    }

    private Matrix4d atgmBarrelTransform(float partialTick) {
        Matrix4d t = getTurretTransform(partialTick);
        t.translate(ATGM_PIVOT[0], ATGM_PIVOT[1], ATGM_PIVOT[2]);
        t.rotate(Axis.XP.rotationDegrees(Mth.lerp(partialTick, getTurretXRotO(), getTurretXRot())));
        return t;
    }

    @Override
    public Matrix4d getTransformFromString(@Nullable String string, float ticks) {
        return ATGM_BARREL.equals(string) ? atgmBarrelTransform(ticks) : super.getTransformFromString(string, ticks);
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
}
