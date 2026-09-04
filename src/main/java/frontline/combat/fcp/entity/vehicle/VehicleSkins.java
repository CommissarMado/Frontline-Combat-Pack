package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.vehicle_skin.SkinInfo;
import com.atsuishio.superbwarfare.data.vehicle_skin.VehicleSkin;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Bridges FCP vehicles onto SBW's vehicle skin system.
 *
 * FCP used to carry its own camo system: a synced int index into a per-entity
 * {@code CAMO_TEXTURES} array, cycled by right-clicking with FCP's own spray. That is now
 * SBW's job - skins are declared in {@code data/fcp/sbw/vehicle_skins/<entity id>.json}, chosen
 * through SBW's skin spray and skin screen, and stored as a synced string id that SBW itself
 * persists as {@code SkinId}.
 *
 * Shared by {@link CamoVehicleBase} and {@link CamoArtilleryBase}, which are siblings under
 * different SBW bases and would otherwise duplicate all of this.
 */
public final class VehicleSkins {

    /** Legacy NBT key holding the pre-skin camo index. Read for migration, never written. */
    private static final String LEGACY_CAMO_KEY = "CamoType";

    /**
     * SBW reserves this id for a vehicle's default skin. Its screen always prepends one entry
     * carrying a blank skin id to stand for the default, then filters the explicitly declared
     * "vanilla" skin out of the list so the same skin is not offered twice. Every FCP vehicle's
     * first skin therefore uses this id - without it the blank entry falls through to
     * {@link #firstSkin} and renders as a duplicate of the second tile.
     */
    private static final String DEFAULT_SKIN_ID = "vanilla";

    /**
     * Deliberately points at nothing: Minecraft resolves an absent texture to the missing-texture
     * checker, which is the correct "this vehicle has no usable skin data" signal. Kept as a plain
     * ResourceLocation rather than MissingTextureAtlasSprite so this stays safe to touch on a
     * dedicated server.
     */
    private static final ResourceLocation MISSING = new ResourceLocation("fcp", "textures/entity/missing.png");

    private VehicleSkins() {
    }

    /**
     * The skin texture this vehicle should render with.
     *
     * Falls back to the first declared skin when the entity's skin id is unset or unknown, so a
     * freshly spawned vehicle - or one whose skin was removed from the datapack - still renders.
     */
    public static ResourceLocation currentTexture(VehicleEntity vehicle) {
        SkinInfo skin = VehicleSkin.getSkin(vehicle);
        if (skin == null) skin = firstSkin(vehicle);
        if (skin == null) return MISSING;
        ResourceLocation texture = ResourceLocation.tryParse(skin.getTexture());
        return texture != null ? texture : MISSING;
    }

    /**
     * Carries a vehicle saved before the skin system over to its equivalent skin.
     *
     * The legacy camo index addressed the same list, in the same order, that the generated skin
     * JSON now declares, so index N maps to the Nth skin. Only applies when SBW has not already
     * loaded a skin id, so it runs once per vehicle and never overwrites a real choice.
     *
     * Call after {@code super.readAdditionalSaveData}, which is what loads {@code SkinId}.
     */
    public static void migrateLegacyCamo(VehicleEntity vehicle, CompoundTag compound) {
        if (!vehicle.getSkinId().isBlank()) return;
        if (!compound.contains(LEGACY_CAMO_KEY)) return;

        List<SkinInfo> skins = skins(vehicle);
        if (skins.isEmpty()) return;

        int index = compound.getInt(LEGACY_CAMO_KEY);
        if (index < 0 || index >= skins.size()) index = 0;

        // Store the default as a blank id rather than "vanilla", matching how SBW's screen
        // represents it, so a migrated vehicle still shows as selected on the default tile.
        String id = skins.get(index).getId();
        vehicle.setSkinId(DEFAULT_SKIN_ID.equals(id) ? "" : id);
    }

    private static SkinInfo firstSkin(VehicleEntity vehicle) {
        List<SkinInfo> skins = skins(vehicle);
        return skins.isEmpty() ? null : skins.get(0);
    }

    private static List<SkinInfo> skins(VehicleEntity vehicle) {
        return VehicleSkin.getSkins(vehicle.getType()).getSkins();
    }
}
