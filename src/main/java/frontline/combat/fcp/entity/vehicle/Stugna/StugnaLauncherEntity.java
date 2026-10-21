package frontline.combat.fcp.entity.vehicle.Stugna;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplacementEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Stugna-P launcher: a remotely operated ATGM emplacement.
 *
 * <p>Nobody ever rides it. A player operates it from a linked {@link StugnaControllerEntity}: their
 * body stays at the control panel and only their camera moves here (see StugnaRemoteClient). To let
 * SuperbWarfare's own turret, weapon, shoot-position and camera code run unchanged, the remote
 * operator is reported as the seat-0 occupant through {@link #getNthEntity} / {@link #getSeatIndex}.
 *
 * <p>Reloading works like the Kornet: walk up and right-click with a missile ({@link EmplacementEntity}).
 */
public class StugnaLauncherEntity extends EmplacementEntity {

    /** UUID of the player remotely operating this launcher, or "" when nobody is. */
    private static final EntityDataAccessor<String> OPERATOR =
            SynchedEntityData.defineId(StugnaLauncherEntity.class, EntityDataSerializers.STRING);

    /**
     * Where the operator's sight is pointing (world yaw / pitch, Minecraft rotation convention). The
     * operator's own body never turns while linked, so the turret follows this instead of their look.
     */
    private static final EntityDataAccessor<Float> AIM_YAW =
            SynchedEntityData.defineId(StugnaLauncherEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_PITCH =
            SynchedEntityData.defineId(StugnaLauncherEntity.class, EntityDataSerializers.FLOAT);

    public StugnaLauncherEntity(EntityType<StugnaLauncherEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OPERATOR, "");
        this.entityData.define(AIM_YAW, 0f);
        this.entityData.define(AIM_PITCH, 0f);
    }

    /** Sets the sight aim; pitch (negative = up) is limited to the turret's elevation range. */
    public void setAim(float yaw, float pitch) {
        this.entityData.set(AIM_YAW, Mth.wrapDegrees(yaw));
        this.entityData.set(AIM_PITCH, clampAimPitch(pitch));
    }

    public float clampAimPitch(float pitch) {
        return Mth.clamp(pitch, -getTurretMaxPitch(), -getTurretMinPitch());
    }

    public Vec3 getAimVector() {
        return Vec3.directionFromRotation(this.entityData.get(AIM_PITCH), this.entityData.get(AIM_YAW));
    }

    /** While operated, the turret follows the operator's sight aim rather than their (fixed) body. */
    @Override
    public void adjustTurretAngle() {
        if (hasOperator()) {
            turretAutoAimFromVector(getAimVector());
        } else {
            super.adjustTurretAngle();
        }
    }

    /** Manual loading only - never pulls ammo from the operator's inventory. */
    @Override
    protected boolean usesInventoryReload() {
        return false;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier().custom((entity, s, dmg) -> getSourceAngle(s, 0.4f) * dmg);
    }

    // ---------------------------------------------------------------------------------------------
    // Remote operator
    // ---------------------------------------------------------------------------------------------

    public boolean hasOperator() {
        return !this.entityData.get(OPERATOR).isEmpty();
    }

    public void setOperator(@Nullable Player player) {
        this.entityData.set(OPERATOR, player == null ? "" : player.getUUID().toString());
    }

    /** The remote operator if they are loaded in this level, else null. Works on both sides. */
    @Nullable
    public Player getOperator() {
        String id = this.entityData.get(OPERATOR);
        if (id.isEmpty()) {
            return null;
        }
        try {
            return this.level().getPlayerByUUID(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isOperator(@Nullable Entity entity) {
        return entity instanceof Player && entity.getUUID().toString().equals(this.entityData.get(OPERATOR));
    }

    // SBW resolves the turret controller, the gunner's weapon data, shoot position and camera through
    // these two; answering with the remote operator makes all of it work without them riding.
    @Override
    public Entity getNthEntity(int index) {
        if (index == 0) {
            Player operator = getOperator();
            if (operator != null) {
                return operator;
            }
        }
        return super.getNthEntity(index);
    }

    @Override
    public int getSeatIndex(Entity entity) {
        if (isOperator(entity)) {
            return 0;
        }
        return super.getSeatIndex(entity);
    }

    /** Operated only through a control panel: never mountable. */
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        // A stale operator (e.g. after a server restart the session map is gone) is cleared.
        if (!this.level().isClientSide() && hasOperator() && !StugnaRemote.isOperating(getOperator(), this)) {
            setOperator(null);
        }
    }
}
