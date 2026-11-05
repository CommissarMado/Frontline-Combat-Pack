package frontline.combat.fcp.entity.vehicle.Leopard2;

import com.atsuishio.superbwarfare.data.vehicle.subdata.SeatInfo;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.vehicle.CamoNetVehicle;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;

import java.util.List;

/**
 * Leopard 2A4, 2A6 and 2A7V on one class; the registry name picks the geo and skins.
 * <p>
 * Crew: 0 driver, 1 gunner (Cannon, Coax), 2 commander, 3 loader. The commander's sight (the "camera" bone) is SBW's
 * passenger weapon station, as on the Puma. The loader's open-top MG3 ("weaponstationturret" yaw,
 * "weaponstationbarrel" pitch) is a second station SBW has no slot for, so - like the Abrams loader's M240 - it is
 * aimed here from the loader's look and exposed to the vehicle JSON as the transform "LoaderBarrel" (the barrel
 * pivot after the ring's yaw and the gun's pitch): the loader's ShootPos and CameraPos use it.
 */
public class Leopard2Entity extends CamoVehicleBase implements CamoNetVehicle {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    public Leopard2Entity(EntityType<Leopard2Entity> type, Level world) {
        super(type, world);
    }

    /** Registry name ("leopard2a4", "leopard2a6", "leopard2a7v"): picks the geo, skins and MG3 pivots. */
    public String variantName() {
        return HumveeAttachments.vehicleName(this.getType());
    }

    // ---- camo net (fcp:camo_net, VehicleCamoNet) ----

    private static final EntityDataAccessor<Integer> CAMO_NET = SynchedEntityData.defineId(Leopard2Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CAMO_NET_COLOR = SynchedEntityData.defineId(Leopard2Entity.class, EntityDataSerializers.INT);
    /** 0 none, 1 light, 2 heavy. */
    private static final int CAMO_NET_VARIANTS = 3;
    private static final int CAMO_NET_COLORS = 5;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CAMO_NET, 0);
        this.entityData.define(CAMO_NET_COLOR, 1);
    }

    @Override public int getCamoNet() { return this.entityData.get(CAMO_NET); }

    @Override
    public int cycleCamoNet() {
        int next = (getCamoNet() + 1) % CAMO_NET_VARIANTS;
        this.entityData.set(CAMO_NET, next);
        return next;
    }

    @Override public int getCamoNetColor() { return this.entityData.get(CAMO_NET_COLOR); }

    @Override
    public int cycleCamoNetColor() {
        int next = (getCamoNetColor() + 1) % CAMO_NET_COLORS;
        this.entityData.set(CAMO_NET_COLOR, next);
        return next;
    }

    @Override public String camoNetName() { return variantName(); }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("CamoNet", getCamoNet());
        compound.putInt("CamoNetColor", getCamoNetColor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("CamoNet")) this.entityData.set(CAMO_NET, compound.getInt("CamoNet") % CAMO_NET_VARIANTS);
        if (compound.contains("CamoNetColor")) this.entityData.set(CAMO_NET_COLOR, compound.getInt("CamoNetColor") % CAMO_NET_COLORS);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        // a destroyed tank loses its camo net (it burns off)
        if (!this.level().isClientSide() && isWreck() && getCamoNet() != 0) this.entityData.set(CAMO_NET, 0);
    }

    // ---- loader's MG3 ----

    /** Seat order: driver, gunner, commander, loader. */
    public static final int LOADER_SEAT = 3;
    public static final String LOADER_BARREL = "LoaderBarrel";

    /** weaponstationturret (ring) and weaponstationbarrel pivots relative to the turret pivot, in blocks (config convention). */
    private static final double[] A4_YAW_POS = {0.5118, 0.9494, -0.6097}, A4_PITCH_POS = {0.2423, 1.1518, -0.3815};
    private static final double[] A6_YAW_POS = {0.5734, 0.9441, -0.6099}, A6_PITCH_POS = {0.3039, 1.1466, -0.3816};
    private static final double[] A7V_YAW_POS = {0.5691, 0.9441, -0.6031}, A7V_PITCH_POS = {0.2996, 1.1466, -0.3748};

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

    /** Built like SBW's station transforms: turret -> ring yaw -> MG pitch. */
    private Matrix4d loaderBarrelTransform(float partialTick) {
        String name = variantName();
        double[] yawPos = name.equals("leopard2a4") ? A4_YAW_POS : name.equals("leopard2a7v") ? A7V_YAW_POS : A6_YAW_POS;
        double[] pitchPos = name.equals("leopard2a4") ? A4_PITCH_POS : name.equals("leopard2a7v") ? A7V_PITCH_POS : A6_PITCH_POS;
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

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
}
