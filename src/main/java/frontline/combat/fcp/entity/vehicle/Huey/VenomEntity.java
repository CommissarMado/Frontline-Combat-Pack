package frontline.combat.fcp.entity.vehicle.Huey;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VenomEntity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    // Same front-camera tracking as VenomGunshipEntity/VenomDoorGunsEntity, applied here for
    // consistency per your request. venom.geo.json doesn't have a "camera" bone yet (it's a new
    // part that only exists in the two new variants' geo files), so VenomModel's "camera" case
    // below never actually fires and nothing visually animates on this variant - but the seat's
    // aim-tracking plumbing (and the copilot's inert "CameraTrack" weapon in venom.json) is wired
    // up the same way, ready for if/when a camera bone gets added to this model too.
    private static final int COPILOT_SEAT = 1;
    private static final Vec3 DEFAULT_CAMERA_DIRECTION = new Vec3(0.140625, 0.351563, 0.522046);

    public VenomEntity(EntityType<VenomEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    public float getCameraYawDeg(float partialTicks) {
        if (getNthEntity(COPILOT_SEAT) == null) return 0f;
        Vec3 targetVec = getShootVec(COPILOT_SEAT, partialTicks);
        if (targetVec == null) return 0f;
        double diffY = Mth.wrapDegrees(-VehicleVecUtils.getYRotFromVector(targetVec) + VehicleVecUtils.getYRotFromVector(DEFAULT_CAMERA_DIRECTION));
        return (float) -diffY;
    }

    public float getCameraPitchDeg(float partialTicks) {
        if (getNthEntity(COPILOT_SEAT) == null) return 0f;
        Vec3 targetVec = getShootVec(COPILOT_SEAT, partialTicks);
        if (targetVec == null) return 0f;
        double diffX = Mth.wrapDegrees(-VehicleVecUtils.getXRotFromVector(targetVec) + VehicleVecUtils.getXRotFromVector(DEFAULT_CAMERA_DIRECTION));
        return (float) -diffX;
    }

}

