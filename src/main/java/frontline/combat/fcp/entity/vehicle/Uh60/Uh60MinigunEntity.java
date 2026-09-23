package frontline.combat.fcp.entity.vehicle.Uh60;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Uh60MinigunEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    // Two independent door guns (right = primary Turret system, left = the
    // PassengerWeaponStation system) each get their own ammo tracker and
    // barrel-spin state, mirroring the single-gun pattern used by
    // ViperEntity / HueyDoorGunnerM134Entity but duplicated per side.
    private int previousRightAmmo = -1;
    private float barrelRotationRight = 0f;
    private float barrelRotationRightOld = 0f;

    private int previousLeftAmmo = -1;
    private float barrelRotationLeft = 0f;
    private float barrelRotationLeftOld = 0f;

    public Uh60MinigunEntity(EntityType<Uh60MinigunEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        barrelRotationRightOld = barrelRotationRight;
        int currentRightAmmo = getAmmoCount("MinigunRight");
        if (previousRightAmmo == -1) {
            previousRightAmmo = currentRightAmmo;
        }
        if (currentRightAmmo < previousRightAmmo) {
            barrelRotationRight += 20f;
            if (barrelRotationRight >= 360f) {
                barrelRotationRight -= 360f;
            }
        }
        previousRightAmmo = currentRightAmmo;

        barrelRotationLeftOld = barrelRotationLeft;
        int currentLeftAmmo = getAmmoCount("MinigunLeft");
        if (previousLeftAmmo == -1) {
            previousLeftAmmo = currentLeftAmmo;
        }
        if (currentLeftAmmo < previousLeftAmmo) {
            barrelRotationLeft += 20f;
            if (barrelRotationLeft >= 360f) {
                barrelRotationLeft -= 360f;
            }
        }
        previousLeftAmmo = currentLeftAmmo;
    }

    public float getBarrelRotRight() {
        return barrelRotationRight;
    }

    public float getBarrelRotRightOld() {
        return barrelRotationRightOld;
    }

    public float getBarrelRotLeft() {
        return barrelRotationLeft;
    }

    public float getBarrelRotLeftOld() {
        return barrelRotationLeftOld;
    }

}
