package frontline.combat.fcp.entity.vehicle.M113;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
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
 * The M113 family (m113, m113a2, skif); the registry name picks the geo and skins. The M113A2's stowage ("toggle1":
 * containers, jerrycan, rucksacks) is a removable hull attachment kept the same way as the Abrams' toggles - fresh
 * spawns roll it once, the spray toggles it afterwards.
 */
public class M113Entity extends CamoVehicleBase implements HumveeVehicle {

    public int INVENTORY_SIZE = 9;

    // Selected attachment variants, encoded as "Category=idx;Category=idx;...".
    private static final EntityDataAccessor<String> ATTACHMENTS = SynchedEntityData.defineId(M113Entity.class, EntityDataSerializers.STRING);
    // Fresh spawns roll a random attachment set once; loaded vehicles keep what was saved.
    private boolean attachmentsInitialized = false;

    public M113Entity(EntityType<M113Entity> type, Level world) {
        super(type, world);
    }

    /** Registry path, e.g. "m113a2". */
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

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!this.level().isClientSide() && !this.attachmentsInitialized) {
            randomizeAttachments();
            this.attachmentsInitialized = true;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Attachments", this.entityData.get(ATTACHMENTS));
        compound.putBoolean("AttachmentsInit", this.attachmentsInitialized);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Attachments")) this.entityData.set(ATTACHMENTS, compound.getString("Attachments"));
        this.attachmentsInitialized = compound.getBoolean("AttachmentsInit") || compound.contains("Attachments");
    }
}
