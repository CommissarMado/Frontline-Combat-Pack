package frontline.combat.fcp.entity.vehicle.Trailers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Tongue point and tow rules for a trailer, from data/<ns>/trailer_towed/<entity_id>.json.
 * tow_x/y/z: tongue in trailer-local space (front tongues use positive z).
 * allowed_drivers / allow_any_driver: which vehicles may tow it.
 * max_articulation: max bend (deg) from the driver's heading (anti-jackknife).
 * attach_search_radius: max hitch-to-tongue distance (blocks) to offer a hitch.
 * terrain_follow: reserved; currently rides at hitch height.
 */
public record TrailerTowedData(
        double towX,
        double towY,
        double towZ,
        List<ResourceLocation> allowedDrivers,
        boolean allowAnyDriver,
        float maxArticulation,
        boolean terrainFollow,
        double attachSearchRadius
) {
    public static final Codec<TrailerTowedData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.optionalFieldOf("tow_x", 0.0).forGetter(TrailerTowedData::towX),
            Codec.DOUBLE.optionalFieldOf("tow_y", 0.5).forGetter(TrailerTowedData::towY),
            Codec.DOUBLE.fieldOf("tow_z").forGetter(TrailerTowedData::towZ),
            ResourceLocation.CODEC.listOf().optionalFieldOf("allowed_drivers", List.of())
                    .forGetter(TrailerTowedData::allowedDrivers),
            Codec.BOOL.optionalFieldOf("allow_any_driver", false).forGetter(TrailerTowedData::allowAnyDriver),
            Codec.FLOAT.optionalFieldOf("max_articulation", 110.0f).forGetter(TrailerTowedData::maxArticulation),
            Codec.BOOL.optionalFieldOf("terrain_follow", false).forGetter(TrailerTowedData::terrainFollow),
            Codec.DOUBLE.optionalFieldOf("attach_search_radius", 6.0)
                    .forGetter(TrailerTowedData::attachSearchRadius)
    ).apply(inst, TrailerTowedData::new));

    /** True if a vehicle with the given registry id is permitted to tow this trailer. */
    public boolean canBeTowedBy(ResourceLocation driverId) {
        return allowAnyDriver || allowedDrivers.contains(driverId);
    }
}