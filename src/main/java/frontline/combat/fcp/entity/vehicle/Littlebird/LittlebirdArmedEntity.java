package frontline.combat.fcp.entity.vehicle.Littlebird;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;

public class LittlebirdArmedEntity extends CamoVehicleBase {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/littlebird/littlebird_armed_1.png"),
            new ResourceLocation("fcp", "textures/entity/littlebird/littlebird_armed_2.png"),
            new ResourceLocation("fcp", "textures/entity/littlebird/littlebird_armed_3.png"),
            new ResourceLocation("fcp", "textures/entity/littlebird/littlebird_armed_4.png")
    };

    private static final String[] CAMO_NAMES = {"Dark", "Light", "Green", "Tan"};

    private static Field propellerRotField;
    private static Field propellerRotOField;

    private int previousCannonAmmo = -1;
    private float barrelRotation = 0f;
    private float barrelRotationOld = 0f;

    static {
        try {
            Class<?> vehicleClass = Class.forName("com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity");
            propellerRotField = vehicleClass.getDeclaredField("propellerRot");
            propellerRotField.setAccessible(true);
            propellerRotOField = vehicleClass.getDeclaredField("propellerRotO");
            propellerRotOField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LittlebirdArmedEntity(EntityType<LittlebirdArmedEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public ResourceLocation[] getCamoTextures() {
        return CAMO_TEXTURES;
    }

    @Override
    public String[] getCamoNames() {
        return CAMO_NAMES;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    public float getPropellerRot() {
        try {
            return propellerRotField != null ? (float) propellerRotField.get(this) : 0f;
        } catch (Exception e) {
            return 0f;
        }
    }

    public float getPropellerRotO() {
        try {
            return propellerRotOField != null ? (float) propellerRotOField.get(this) : 0f;
        } catch (Exception e) {
            return 0f;
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // Store previous barrel rotation for smooth interpolation
        barrelRotationOld = barrelRotation;

        // Check if cannon ammo has changed (meaning it was fired)
        int currentAmmo = getAmmoCount("Cannon");

        // Initialize on first tick
        if (previousCannonAmmo == -1) {
            previousCannonAmmo = currentAmmo;
        }

        // If ammo decreased, increment barrel rotation
        if (currentAmmo < previousCannonAmmo) {
            barrelRotation += 20f; // Increment by 20 degrees per shot
            if (barrelRotation >= 360f) {
                barrelRotation -= 360f; // Wrap around at 360 degrees
            }
        }

        // Update stored ammo count for next tick
        previousCannonAmmo = currentAmmo;
    }

    public boolean GetWeaponState(String WeaponName, int Count) {
        if (getAmmoCount(WeaponName) == Count)
            return true;
        else if (getAmmoCount(WeaponName) < Count)
            return true;
        else
            return false;

    }

    public float getBarrelRot() {
        return barrelRotation;
    }

    public float getBarrelRot0() {
        return barrelRotationOld;
    }
}
