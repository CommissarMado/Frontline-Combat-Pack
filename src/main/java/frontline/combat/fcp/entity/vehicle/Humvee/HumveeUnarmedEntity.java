package frontline.combat.fcp.entity.vehicle.Humvee;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import frontline.combat.fcp.vehicle.humvee.HumveeVehicle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One entity class shared by every unarmed HMMWV variant. Each variant is a separate
 * EntityType registered against this class; the variant is identified at runtime by its
 * registry name (see {@link #humveeName()}), which selects the geo, textures and
 * attachment data. Turret/weapon handling is intentionally omitted for now.
 */
public class HumveeUnarmedEntity extends CamoVehicleBase implements HumveeVehicle {

    // Shared camo skins (paint schemes over the common UV). Wrecked slots reuse the normal
    // skins as placeholders until dedicated wrecked textures exist. camoCount = 11.
    private static final String[] SKINS = {
            "hmww_tex_1", "hmww_tex_1_1", "hmww_tex_2", "hmww_tex_2_1", "hmww_tex_3_1",
            "hmww_tex_5", "hmww_tex_6", "hmww_tex_6_1", "hmww_tex_6_2",
            "hmww_tex_7_komuf_1", "hmww_tex_7_komuf_2"
    };
    private static final ResourceLocation[] CAMO_TEXTURES = buildTextures();
    private static final String[] CAMO_NAMES = {
            "Tex 1", "Tex 1-1", "Tex 2", "Tex 2-1", "Tex 3-1", "Tex 5",
            "Tex 6", "Tex 6-1", "Tex 6-2", "Komuf 1", "Komuf 2"
    };

    private static ResourceLocation[] buildTextures() {
        ResourceLocation[] out = new ResourceLocation[SKINS.length * 2];
        for (int i = 0; i < SKINS.length; i++) {
            out[i] = new ResourceLocation("fcp", "textures/entity/humvee/" + SKINS[i] + ".png");
            out[i + SKINS.length] = out[i]; // wrecked placeholder = normal
        }
        return out;
    }

    private static final EntityDataAccessor<Float> STEERING_ANGLE =
            SynchedEntityData.defineId(HumveeUnarmedEntity.class, EntityDataSerializers.FLOAT);
    // Selected attachment variants, encoded as "Category=idx;Category=idx;...".
    private static final EntityDataAccessor<String> ATTACHMENTS =
            SynchedEntityData.defineId(HumveeUnarmedEntity.class, EntityDataSerializers.STRING);

    private float prevSteeringAngle = 0f;
    private float wheelRotation = 0f;
    private float prevWheelRotation = 0f;

    public HumveeUnarmedEntity(EntityType<HumveeUnarmedEntity> type, Level world) {
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
    public String humveeName() {
        return HumveeAttachments.vehicleName(this.getType());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STEERING_ANGLE, 0f);
        this.entityData.define(ATTACHMENTS, "");
    }

    // ---- attachment state ----

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
        int next = (map.getOrDefault(category, 0) + 1) % variantCount;
        map.put(category, next);
        writeAttachments(map);
    }

    // ---- steering / wheels (identical to the base Humvee) ----

    public float getSteeringAngle() {
        return this.entityData.get(STEERING_ANGLE);
    }

    public void setSteeringAngle(float angle) {
        this.entityData.set(STEERING_ANGLE, angle);
    }

    public float getPrevSteeringAngle() {
        return prevSteeringAngle;
    }

    public float getWheelRotation() {
        return wheelRotation;
    }

    public float getPrevWheelRotation() {
        return prevWheelRotation;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SteeringAngle", this.getSteeringAngle());
        compound.putString("Attachments", this.entityData.get(ATTACHMENTS));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SteeringAngle")) {
            setSteeringAngle(compound.getFloat("SteeringAngle"));
        }
        if (compound.contains("Attachments")) {
            this.entityData.set(ATTACHMENTS, compound.getString("Attachments"));
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();

        prevSteeringAngle = getSteeringAngle();
        float currentAngle = getSteeringAngle();

        double speed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x
                + this.getDeltaMovement().z * this.getDeltaMovement().z);
        boolean isMoving = speed > 0.05;
        boolean turningLeft = this.leftInputDown();
        boolean turningRight = this.rightInputDown();

        if (turningLeft && !turningRight) {
            currentAngle = Math.min(45f, currentAngle + 2.0f);
            setSteeringAngle(currentAngle);
        } else if (turningRight && !turningLeft) {
            currentAngle = Math.max(-45f, currentAngle - 2.0f);
            setSteeringAngle(currentAngle);
        } else if (isMoving && Math.abs(currentAngle) > 0.5f) {
            currentAngle *= 0.9f;
            setSteeringAngle(currentAngle);
        }

        if (isMoving && Math.abs(currentAngle) > 1f) {
            this.setYRot(this.getYRot() + currentAngle * 0.009f * (float) speed);
        }

        prevWheelRotation = wheelRotation;
        double forwardComponent = this.getDeltaMovement().x * Math.sin(Math.toRadians(this.getYRot()))
                + this.getDeltaMovement().z * Math.cos(Math.toRadians(this.getYRot()));
        if (forwardComponent > 0) {
            wheelRotation += (float) (speed * 20f);
        } else if (forwardComponent < 0) {
            wheelRotation -= (float) (speed * 20f);
        }
    }
}
