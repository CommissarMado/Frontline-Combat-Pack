package frontline.combat.fcp.entity.vehicle.Emplacement;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import com.atsuishio.superbwarfare.tools.VectorTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;

import java.util.List;

/**
 * Base for the limited-FOV emplacements (Mk19, M2, AGS-17, ZiS-3). Adds two INTERACTIVE hitboxes at
 * the tripod's rear legs — like the Type-63 MLRS: shift + right-click a rear leg to spin the whole
 * emplacement (BODY_YAW builds up with the hold, decays, and is fed into the entity yaw each tick).
 * Reload is inherited from EmplacementEntity but gated off by default here (see needsManualReload).
 */
public abstract class ClampedTurretEntity extends EmplacementEntity {
    private static final EntityDataAccessor<Float> BODY_YAW =
            SynchedEntityData.defineId(ClampedTurretEntity.class, EntityDataSerializers.FLOAT);

    public OBB body; // solid collision box (the green box IS the collision in SBW's OBB system)
    public OBB leg1; // right rear leg (+X)
    public OBB leg2; // left rear leg (-X)
    private double interactionTick;

    public ClampedTurretEntity(EntityType<? extends VehicleEntity> type, Level world) {
        super(type, world);
        double[] bb = bodyBox();
        this.body = new OBB(OBB.vec3ToVector3d(this.position()), new Vector3d(bb[0], bb[1], bb[2]), new Quaterniond(), OBB.Part.BODY);
        this.leg1 = new OBB(OBB.vec3ToVector3d(this.position()), new Vector3d(0.2, 0.35, 0.2), new Quaterniond(), OBB.Part.INTERACTIVE);
        this.leg2 = new OBB(OBB.vec3ToVector3d(this.position()), new Vector3d(0.2, 0.35, 0.2), new Quaterniond(), OBB.Part.INTERACTIVE);
    }

    /** Local [x, y, z] of the right rear leg; the left leg mirrors -x. Bigger for the ZiS-3. */
    protected abstract double[] legOffset();

    /** Collision box: {halfX, halfY, halfZ, centerX, centerY, centerZ}. Override for larger turrets. */
    protected double[] bodyBox() { return new double[]{0.4, 0.42, 0.4, 0.0, 0.42, 0.0}; }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BODY_YAW, 0f);
    }

    public float getBodyYaw() { return this.entityData.get(BODY_YAW); }
    public void setBodyYaw(float v) { this.entityData.set(BODY_YAW, v); }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.getMainHandItem().isEmpty() && player.isShiftKeyDown()) {
            OBB looking = OBB.getLookingObb(player, player.getEntityReach());
            if (looking == leg1 || looking == leg2) {
                if (player.level() instanceof ServerLevel) {
                    float dir = (looking == leg1) ? 0.2f : -0.2f;
                    setBodyYaw(getBodyYaw() + dir * (float) interactionTick);
                    interactionTick++;
                }
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
        }
        return super.interact(player, hand);
    }

    @Override
    public List<OBB> getOBBs() {
        List<OBB> list = new java.util.ArrayList<>(super.getOBBs());
        if (body != null) list.add(body);
        if (leg1 != null) list.add(leg1);
        if (leg2 != null) list.add(leg2);
        return list;
    }

    @Override
    public void updateOBB() {
        super.updateOBB();
        if (leg1 == null || leg2 == null || body == null) return;
        Matrix4d transform = getVehicleTransform(1);
        double[] bb = bodyBox();
        Vector4d wb = transformPosition(transform, bb[3], bb[4], bb[5]);
        this.body.center.set(new Vector3f((float) wb.x, (float) wb.y, (float) wb.z));
        this.body.updateRotation(VectorTool.combineRotations(1, this));
        double[] o = legOffset();
        Vector4d w1 = transformPosition(transform, o[0], o[1], o[2]);
        this.leg1.center.set(new Vector3f((float) w1.x, (float) w1.y, (float) w1.z));
        this.leg1.updateRotation(VectorTool.combineRotations(1, this));
        Vector4d w2 = transformPosition(transform, -o[0], o[1], o[2]);
        this.leg2.center.set(new Vector3f((float) w2.x, (float) w2.y, (float) w2.z));
        this.leg2.updateRotation(VectorTool.combineRotations(1, this));
    }

    @Override
    public void baseTick() {
        super.baseTick();
        interactionTick *= 0.94;
        setBodyYaw(getBodyYaw() * 0.8f);
        setYRot(getYRot() + getBodyYaw());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag c) {
        super.addAdditionalSaveData(c);
        c.putFloat("BodyYaw", getBodyYaw());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag c) {
        super.readAdditionalSaveData(c);
        if (c.contains("BodyYaw")) setBodyYaw(c.getFloat("BodyYaw"));
    }
}