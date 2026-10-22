package frontline.combat.fcp.entity.vehicle.Abrams;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.data.vehicle.subdata.SeatInfo;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import java.util.List;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import frontline.combat.fcp.vehicle.humvee.HumveeVehicle;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One entity class for every Abrams variant (m1a1, m1a1_aim, m1a1_ukr, m1a2, m1a2_tusk1, m1a2_tusk2).
 * Each is its own EntityType on this class; the registry name picks the geo, skins and attachment
 * data. The "toggle" bones are removable attachments, kept the same way as the Humvees'.
 */
public class M1a1Entity extends CamoVehicleBase implements HumveeVehicle {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(M1a1Entity.class, EntityDataSerializers.FLOAT);
    // Selected attachment variants, encoded as "Category=idx;Category=idx;...".
    private static final EntityDataAccessor<String> ATTACHMENTS = SynchedEntityData.defineId(M1a1Entity.class, EntityDataSerializers.STRING);
    // Fresh spawns roll a random attachment set once; loaded vehicles keep what was saved.
    private boolean attachmentsInitialized = false;

    private float prevSteeringAngle = 0f;

    private float wheelRotation = 0f;

    private float prevWheelRotation = 0f;

    public M1a1Entity(EntityType<M1a1Entity> type, Level world) {super(type, world);}

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STEERING_ANGLE, 0f);
        this.entityData.define(ATTACHMENTS, "");
    }

    // ---- loader's MG (turret3 / barrel3) ----
    // SBW has no slot for a third turret, so the loader MG is aimed here and exposed to the vehicle
    // JSON as the transform "LoaderBarrel" (barrel3's pivot, after turret3 yaw and barrel3 pitch):
    // the loader's ShootPos and CameraPos use it, so muzzle and camera move with the gun.

    /** Seat order: driver, gunner, commander station, loader. */
    public static final int LOADER_SEAT = 3;
    public static final String LOADER_BARREL = "LoaderBarrel";

    /** turret3 pivot relative to the turret, and barrel3 pivot relative to the turret (blocks, from the 0.8x-resized geos). */
    private static final double[] LOADER_YAW_POS = {0.4126, 0.2166, -0.7731}, LOADER_PITCH_POS = {0.4426, 0.4666, -0.2482};
    private static final double[] AIM_LOADER_YAW_POS = {0.4226, 0.2166, -0.7931}, AIM_LOADER_PITCH_POS = {0.4376, 0.5116, -0.3282};

    /**
     * Loader's look relative to the turret, {yaw, pitch} in degrees (Minecraft signs: +yaw right,
     * +pitch down), clamped to the loader seat's limits - the same limits SBW applies to the view.
     */
    public float[] loaderAim(float partialTick) {
        Entity loader = getNthEntity(LOADER_SEAT);
        List<SeatInfo> seats = computed().seats();
        if (loader == null || seats.size() <= LOADER_SEAT) return new float[]{0f, 0f};
        SeatInfo seat = seats.get(LOADER_SEAT);
        Vec3 turret = getTurretVector(partialTick);
        float turretYaw = (float) (-Mth.atan2(turret.x, turret.z) * Mth.RAD_TO_DEG);
        float turretPitch = (float) (-Mth.atan2(turret.y, turret.horizontalDistance()) * Mth.RAD_TO_DEG);
        float yaw = Mth.clamp(Mth.wrapDegrees(loader.getViewYRot(partialTick) - turretYaw), seat.getMinYaw(), seat.getMaxYaw());
        float pitch = Mth.clamp(loader.getViewXRot(partialTick) - turretPitch, -seat.getMaxPitch(), -seat.getMinPitch());
        return new float[]{yaw, pitch};
    }

    /** Built like SBW's commander-station transforms: turret -> turret3 yaw -> barrel3 pitch. */
    private Matrix4d loaderBarrelTransform(float partialTick) {
        boolean aim = "m1a1_aim".equals(humveeName());
        double[] yawPos = aim ? AIM_LOADER_YAW_POS : LOADER_YAW_POS, pitchPos = aim ? AIM_LOADER_PITCH_POS : LOADER_PITCH_POS;
        float[] a = loaderAim(partialTick);
        Matrix4d t = getTurretTransform(partialTick);
        t.translate(yawPos[0], yawPos[1], yawPos[2]);
        t.rotate(Axis.YP.rotationDegrees(-a[0]));
        t.translate(pitchPos[0] - yawPos[0], pitchPos[1] - yawPos[1], pitchPos[2] - yawPos[2]);
        t.rotate(Axis.XP.rotationDegrees(a[1]));
        return t;
    }

    @Override
    public Matrix4d getTransformFromString(@Nullable String string, float ticks) {
        return LOADER_BARREL.equals(string) ? loaderBarrelTransform(ticks) : super.getTransformFromString(string, ticks);
    }

    /** The loader turns his whole body with the MG, not just his head. */
    @Override
    public void copyEntityData(Entity entity) {
        super.copyEntityData(entity);
        if (getSeatIndex(entity) == LOADER_SEAT) {
            Vec3 turret = getTurretVector(1f);
            float turretYaw = (float) (-Mth.atan2(turret.x, turret.z) * Mth.RAD_TO_DEG);
            entity.setYBodyRot(turretYaw + loaderAim(1f)[0]);
        }
    }

    // ---- attachment state (same encoding as the Humvees) ----

    @Override
    public String humveeName() {
        return HumveeAttachments.vehicleName(this.getType());
    }

    private Map<String, Integer> parseAttachments() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String raw = this.entityData.get(ATTACHMENTS);
        if (raw == null || raw.isEmpty()) return map;
        for (String part : raw.split(";")) {
            int eq = part.indexOf('=');
            if (eq > 0) {
                try {
                    map.put(part.substring(0, eq), Integer.parseInt(part.substring(eq + 1)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }

    private void writeAttachments(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        this.entityData.set(ATTACHMENTS, sb.toString());
    }

    @Override
    public int getAttachmentIndex(String category) {
        return parseAttachments().getOrDefault(category, 0);
    }

    @Override
    public void cycleAttachment(String category, int variantCount) {
        if (variantCount <= 0) return;
        Map<String, Integer> map = parseAttachments();
        map.put(category, (map.getOrDefault(category, 0) + 1) % variantCount);
        writeAttachments(map);
    }

    /** Pick a random variant for every category. Called once, on first spawn. */
    private void randomizeAttachments() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (HumveeAttachments.Category c : HumveeAttachments.categories(humveeName())) {
            if (c.variantCount > 0) map.put(c.name, this.random.nextInt(c.variantCount));
        }
        writeAttachments(map);
    }

    public float getSteeringAngle() {
        return this.entityData.get(STEERING_ANGLE);
    }

    public void setSteeringAngle(float angle) {this.entityData.set(STEERING_ANGLE, angle);
    }

    public float getPrevSteeringAngle(){
        return prevSteeringAngle;
    }

    public float getWheelRotation(){
        return wheelRotation;
    }

    public float getPrevWheelRotation(){
        return prevWheelRotation;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SteeringAngle", this.getSteeringAngle());
        compound.putString("Attachments", this.entityData.get(ATTACHMENTS));
        compound.putBoolean("AttachmentsInit", this.attachmentsInitialized);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SteeringAngle")) {
            setSteeringAngle(compound.getFloat("SteeringAngle"));
        }
        if (compound.contains("Attachments")) {
            this.entityData.set(ATTACHMENTS, compound.getString("Attachments"));
        }
        this.attachmentsInitialized = compound.getBoolean("AttachmentsInit") || compound.contains("Attachments");
    }
    public boolean GetWeaponState(String WeaponName, int Count) {
        if (getAmmoCount(WeaponName) == Count)
            return true;
        else if (getAmmoCount(WeaponName) < Count)
            return true;
        else
            return false;

    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!this.level().isClientSide() && !this.attachmentsInitialized) {
            randomizeAttachments();
            this.attachmentsInitialized = true;
        }

        prevSteeringAngle = getSteeringAngle();
        float currentAngle = getSteeringAngle();

        double speed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);

        boolean isMoving = speed > 0.05;

        boolean turningLeft = this.leftInputDown();
        boolean turningRight = this.rightInputDown();

        if (turningLeft && !turningRight) {
            currentAngle += 2.0f;
            currentAngle = Math.min(45f, currentAngle);
            setSteeringAngle(currentAngle);
        } else if (turningRight && !turningLeft) {
            currentAngle -= 2.0f;
            currentAngle = Math.max(-45f, currentAngle);
            setSteeringAngle(currentAngle);
        } else if (isMoving && Math.abs(currentAngle) > 0.5f) {
            currentAngle *= 0.9f;
            setSteeringAngle(currentAngle);
        }

        if (isMoving && Math.abs(currentAngle) > 1f) {
            float turnAmount = currentAngle * 0.009f * (float)speed;
            this.setYRot(this.getYRot() + turnAmount);
        }

        prevWheelRotation = wheelRotation;
        wheelRotation += (float) (speed * 20f);
    }
}
