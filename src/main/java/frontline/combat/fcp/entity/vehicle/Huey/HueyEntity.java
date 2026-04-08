package frontline.combat.fcp.entity.vehicle.Huey;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;

public class HueyEntity extends CamoVehicleBase {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/huey/huey_1.png"),
            new ResourceLocation("fcp", "textures/entity/huey/huey_2.png"),
            new ResourceLocation("fcp", "textures/entity/huey/huey_3.png")
    };
    private static final String[] CAMO_NAMES = {"Standard", "White", "Shark"};

    private static Field propellerRotField;
    private static Field propellerRotOField;

    private int previousGunner1Ammo = -1;
    private int previousGunner2Ammo = -1;
    private float barrel1Rotation = 0f;
    private float barrel1RotationOld = 0f;

    private float barrel2Rotation = 0f;
    private float barrel2RotationOld = 0f;

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

    public HueyEntity(EntityType<HueyEntity> type, Level world) {
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
        barrel1RotationOld = barrel1Rotation;
        barrel2RotationOld = barrel2Rotation;

        // Check if cannon ammo has changed (meaning it was fired)
        int currentAmmo1 = getAmmoCount("Gunner1");
        int currentAmmo2 = getAmmoCount("Gunner2");

        // Initialize on first tick
        if (previousGunner1Ammo == -1) {
            previousGunner1Ammo = currentAmmo1;
        }

        if (previousGunner2Ammo == -1) {
            previousGunner2Ammo = currentAmmo2;
        }

        // If ammo decreased, increment barrel rotation
        if (currentAmmo1 < previousGunner1Ammo) {
            barrel1Rotation -= 20f; // Increment by 20 degrees per shot
            if (barrel1Rotation <= 0) {
                barrel1Rotation += 360f; // Wrap around at 360 degrees
            }
        }

        if (currentAmmo2 < previousGunner2Ammo) {
            barrel2Rotation += 20f; // Increment by 20 degrees per shot
            if (barrel2Rotation >= 360f) {
                barrel2Rotation -= 360f; // Wrap around at 360 degrees
            }
        }

        // Update stored ammo count for next tick
        previousGunner1Ammo = currentAmmo1;
        previousGunner2Ammo = currentAmmo2;
    }

    public boolean GetWeaponState(String WeaponName, int Count) {
        if (getAmmoCount(WeaponName) == Count)
            return true;
        else if (getAmmoCount(WeaponName) < Count)
            return true;
        else
            return false;

    }

    public float getBarrel1Rot() {return barrel1Rotation;}

    public float getBarrel2Rot() {return barrel2Rotation;}

    public float getBarrel1Rot0() {
        return barrel1RotationOld;
    }

    public float getBarrel2Rot0() {
        return barrel2RotationOld;
    }

}
