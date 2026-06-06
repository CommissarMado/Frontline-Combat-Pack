package frontline.combat.fcp.entity.vehicle.MemeVehicles;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;

public class LaHumveeEntity extends CamoVehicleBase {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            //Normal Texture
            new ResourceLocation("fcp", "textures/entity/humvee/humvee_m2_green.png"),
            new ResourceLocation("fcp", "textures/entity/humvee/humvee_m2_iraq.png"),
            new ResourceLocation("fcp", "textures/entity/humvee/humvee_m2_ukr.png"),
            //Wrecked Texture
            new ResourceLocation("fcp", "textures/entity/humvee/humvee_m2_green_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/humvee/humvee_m2_iraq_wrecked.png"),
            new ResourceLocation("fcp", "textures/entity/humvee/humvee_m2_ukr_wrecked.png")
    };

    private static final String[] CAMO_NAMES = {"Green", "Iraq", "Ukrainian"};

    private static Field propellerRotField;
    private static Field propellerRotOField;

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

    public LaHumveeEntity(EntityType<LaHumveeEntity> type, Level world) {
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
}
