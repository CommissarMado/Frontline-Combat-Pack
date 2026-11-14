package frontline.combat.fcp.entity.vehicle.Stryker;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoArtilleryBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class StrykerMortarEntity extends CamoArtilleryBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    /** Ticks between the shell-drop + fire sounds and the round leaving the tube (SBW's deployable mortar: FIRE_TIME
     * 25 -> spawn at 5); CamoArtilleryBase delays every shot, the gunner's and the Artillery Indicator's. */
    @Override
    protected int fireDelayTicks() {
        return 20;
    }

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(StrykerMortarEntity.class, EntityDataSerializers.FLOAT);
    private float prevSteeringAngle = 0f, wheelRotation = 0f, prevWheelRotation = 0f;

    public StrykerMortarEntity(EntityType<StrykerMortarEntity> type, Level world) { super(type, world); registerMgTransforms(); }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(STEERING_ANGLE, 0f); }
    public float getSteeringAngle() { return this.entityData.get(STEERING_ANGLE); }
    public void setSteeringAngle(float a) { this.entityData.set(STEERING_ANGLE, a); }
    public float getPrevSteeringAngle(){ return prevSteeringAngle; }
    public float getWheelRotation(){ return wheelRotation; }
    public float getPrevWheelRotation(){ return prevWheelRotation; }

    // ---- roof M240: bound bones, like the UH-60's door guns (Uh60MinigunEntity) ----
    // Not SBW's passenger weapon station: that hangs off the turret (here the 360-degree mortar) and SBW feeds the
    // turret's yaw into the gun. The gun aims straight from its seat's look - SBW clamps that to the seat's
    // MinYaw/MaxYaw (+-70, the Type 10 roof MG's arc) and pitch limits - against the weapon's rest direction, and the
    // "MgBarrel" (ShootPos) / "MgTurret" (camera) transforms are rooted in the hull. StrykerMortarModel turns
    // turret2/barrel2 by the same angles.

    public static final int MG_SEAT = 2;
    /** Pintle pivot (vehicle space, blocks) - the yaw and pitch pivot of turret2/barrel2 in the geo. */
    private static final net.minecraft.world.phys.Vec3 MG_PIVOT = new net.minecraft.world.phys.Vec3(-0.5465, 2.68, 0.269);

    private void registerMgTransforms() {
        getPositionTransform().put("MgBarrel", (Float pt) -> mgTransform(pt, true));
        getVectorTransform().put("MgBarrel", (Float pt) -> {
            org.joml.Matrix4d t = mgTransform(pt, true);
            org.joml.Vector4d root = t.transform(new org.joml.Vector4d(0, 0, 0, 1)), tip = t.transform(new org.joml.Vector4d(0, 0, 1, 1));
            return new net.minecraft.world.phys.Vec3(tip.x - root.x, tip.y - root.y, tip.z - root.z);
        });
        getPositionTransform().put("MgTurret", (Float pt) -> mgTransform(pt, false));
    }

    /** Gun yaw (degrees) from rest to the gunner's aim - BoundBonesYaw's math. */
    public float mgYawDeg(float partialTicks) {
        if (getNthEntity(MG_SEAT) == null) return 0f;
        net.minecraft.world.phys.Vec3 aim = getShootVec(MG_SEAT, partialTicks), rest = getDefaultBarrelDirection(MG_SEAT, partialTicks);
        if (aim == null || rest == null) return 0f;
        return (float) net.minecraft.util.Mth.wrapDegrees(com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils.getYRotFromVector(aim)
                - com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils.getYRotFromVector(rest));
    }

    /** Gun pitch (degrees, the bone's sense) from rest to the gunner's aim - BoundBonesPitch's math. */
    public float mgPitchDeg(float partialTicks) {
        if (getNthEntity(MG_SEAT) == null) return 0f;
        net.minecraft.world.phys.Vec3 aim = getShootVec(MG_SEAT, partialTicks), rest = getDefaultBarrelDirection(MG_SEAT, partialTicks);
        if (aim == null || rest == null) return 0f;
        return (float) net.minecraft.util.Mth.wrapDegrees(com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils.getXRotFromVector(aim)
                - com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils.getXRotFromVector(rest));
    }

    /** Hull -> pintle -> gun yaw (-> gun pitch); JOML's X turns opposite to the bone's, as on the UH-60. */
    private org.joml.Matrix4d mgTransform(float partialTicks, boolean pitched) {
        org.joml.Matrix4d t = getVehicleTransformWithCustomPitch(partialTicks);
        t.translate(MG_PIVOT.x, MG_PIVOT.y, MG_PIVOT.z);
        t.rotate(com.mojang.math.Axis.YP.rotationDegrees(mgYawDeg(partialTicks)));
        if (pitched) t.rotate(com.mojang.math.Axis.XP.rotationDegrees(-mgPitchDeg(partialTicks)));
        return t;
    }


    @Override public DamageModifier getDamageModifier() {
        return super.getDamageModifier().custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag c) { super.addAdditionalSaveData(c); c.putFloat("SteeringAngle", getSteeringAngle()); }
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag c) { super.readAdditionalSaveData(c); if (c.contains("SteeringAngle")) setSteeringAngle(c.getFloat("SteeringAngle")); }

    @Override public void baseTick() {
        super.baseTick();

        prevSteeringAngle = getSteeringAngle();
        float a = getSteeringAngle();
        double speed = Math.sqrt(getDeltaMovement().x*getDeltaMovement().x + getDeltaMovement().z*getDeltaMovement().z);
        boolean moving = speed > 0.05;
        if (leftInputDown() && !rightInputDown()) { a = Math.min(45f, a+2f); setSteeringAngle(a); }
        else if (rightInputDown() && !leftInputDown()) { a = Math.max(-45f, a-2f); setSteeringAngle(a); }
        else if (moving && Math.abs(a) > 0.5f) { a *= 0.9f; setSteeringAngle(a); }
        if (moving && Math.abs(a) > 1f) setYRot(getYRot() + a*0.009f*(float)speed);
        prevWheelRotation = wheelRotation; wheelRotation += (float)(speed*20f);
    }
}
