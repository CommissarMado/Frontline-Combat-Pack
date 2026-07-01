package frontline.combat.fcp.entity.vehicle.Mi17;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.entity.vehicle.Viper.ViperEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;

public class MI17Entity extends CamoVehicleBase {

    private static final ResourceLocation[] CAMO_TEXTURES = {
            // Normal camos [0-2]
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_1.png"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_2.png"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_3.png"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_4.png"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_5.png"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_6.png"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_7.png"),
            // Wrecked variants [3-5], one per camo in the same order
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_1.png_wrecked"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_2.png_wrecked"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_3.png_wrecked"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_4.png_wrecked"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_5.png_wrecked"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_6.png_wrecked"),
            new ResourceLocation("fcp", "textures/entity/mi17/mi17_7.png_wrecked"),
    };
    private static final String[] CAMO_NAMES = {"Standard", "White", "Shark"};

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

    public MI17Entity(EntityType<MI17Entity> type, Level world) {
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
