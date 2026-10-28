package frontline.combat.fcp.entity.vehicle.T90M;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.entity.vehicle.RoofCover;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import frontline.combat.fcp.vehicle.humvee.HumveeVehicle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * T-90M family (t90m, t90m_2022, t90m_2024, t90ms); the registry name picks the geo and skins. The commander
 * has a Kord on a remote weapon station (SBW's passenger weapon station), viewed like the M1A2's CROWS.
 * <p>
 * The 2022 and 2024 carry the "mangal" cope cage, a turret attachment toggled with the spray like the Humvee
 * attachments ({@link HumveeVehicle}); it starts off and is never randomised. While it's fitted, every hit coming in
 * from above does half damage ({@link RoofCover}, applied by VehicleArmor with or without the armour model).
 */
public class T90MEntity extends CamoVehicleBase implements HumveeVehicle, RoofCover {

    /** The cope cage attachment (bone "managal"). Index 0 = fitted, 1 = removed. */
    public static final String MANGAL = "managal";
    private static final int FITTED = 0, REMOVED = 1;

    private static final EntityDataAccessor<String> ATTACHMENTS = SynchedEntityData.defineId(T90MEntity.class, EntityDataSerializers.STRING);

    public int INVENTORY_SIZE = 9;

    public T90MEntity(EntityType<T90MEntity> type, Level world) {
        super(type, world);
    }

    /** Registry path, e.g. "t90m_2022". */
    public String variant() {
        return EntityType.getKey(getType()).getPath();
    }

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACHMENTS, "");
    }

    // ---- mangal attachment (Humvee attachment encoding, but "removed" by default) ----

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
        return parseAttachments().getOrDefault(category, REMOVED);
    }

    @Override
    public void cycleAttachment(String category, int variantCount) {
        if (variantCount <= 0) return;
        Map<String, Integer> map = parseAttachments();
        map.put(category, (map.getOrDefault(category, REMOVED) + 1) % variantCount);
        writeAttachments(map);
    }

    /** True while the cope cage is fitted (only the variants that have one can fit it). */
    public boolean cageFitted() {
        return !HumveeAttachments.categories(humveeName()).isEmpty() && getAttachmentIndex(MANGAL) == FITTED;
    }

    /** Damage multiplier for hits from above while the cope cage is fitted (slat + ERA over the roof). */
    public static final float CAGE_ROOF_MULTIPLIER = 0.5f;

    @Override
    public float roofDamageMultiplier() {
        return cageFitted() ? CAGE_ROOF_MULTIPLIER : 1f;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Attachments", this.entityData.get(ATTACHMENTS));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Attachments")) this.entityData.set(ATTACHMENTS, compound.getString("Attachments"));
    }
}
