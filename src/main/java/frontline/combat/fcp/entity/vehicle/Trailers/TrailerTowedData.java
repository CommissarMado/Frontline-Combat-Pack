package frontline.combat.fcp.entity.vehicle.Trailers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * TrailerTowedData — the towed (tongue) point on a TRAILER, plus the rules for
 * which vehicles may tow it and how it articulates.
 *
 * One JSON file per trailer entity, placed at:
 *   data/<namespace>/trailer_towed/<entity_id>.json
 * where <entity_id> is the trailer's registry name (e.g. "example_trailer").
 *
 * The tongue point is expressed in the trailer's LOCAL space and is the point
 * that gets pinned to the driver's hitch point:
 *   tow_x = lateral offset  (+right, -left)        — usually 0
 *   tow_y = vertical offset (+up)
 *   tow_z = longitudinal    (+forward, -backward)  — usually positive (front tongue)
 *
 * Whitelist:
 *   allowed_drivers   = list of vehicle registry ids that may tow this trailer
 *   allow_any_driver  = if true, any vehicle with trailer_driver data may tow it
 *
 * Behaviour:
 *   max_articulation  = max angle (deg) the trailer may bend away from the
 *                       driver's heading; prevents violent jackknife flips.
 *   terrain_follow    = if true the trailer rides at hitch height; reserved for
 *                       future ground-snapping. Currently rides at the hitch.
 *
 * Example (data/fcp/trailer_towed/example_trailer.json):
 * {
 *   "tow_x": 0.0,
 *   "tow_y": 0.4,
 *   "tow_z": 3.0,
 *   "allowed_drivers": [ "fcp:kamaz", "fcp:matv" ],
 *   "allow_any_driver": false,
 *   "max_articulation": 110.0,
 *   "terrain_follow": false
 * }
 */
public record TrailerTowedData(
        double towX,
        double towY,
        double towZ,
        List<ResourceLocation> allowedDrivers,
        boolean allowAnyDriver,
        float maxArticulation,
        boolean terrainFollow
) {
    public static final Codec<TrailerTowedData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.optionalFieldOf("tow_x", 0.0).forGetter(TrailerTowedData::towX),
            Codec.DOUBLE.optionalFieldOf("tow_y", 0.5).forGetter(TrailerTowedData::towY),
            Codec.DOUBLE.fieldOf("tow_z").forGetter(TrailerTowedData::towZ),
            ResourceLocation.CODEC.listOf().optionalFieldOf("allowed_drivers", List.of())
                    .forGetter(TrailerTowedData::allowedDrivers),
            Codec.BOOL.optionalFieldOf("allow_any_driver", false).forGetter(TrailerTowedData::allowAnyDriver),
            Codec.FLOAT.optionalFieldOf("max_articulation", 110.0f).forGetter(TrailerTowedData::maxArticulation),
            Codec.BOOL.optionalFieldOf("terrain_follow", false).forGetter(TrailerTowedData::terrainFollow)
    ).apply(inst, TrailerTowedData::new));

    /** True if a vehicle with the given registry id is permitted to tow this trailer. */
    public boolean canBeTowedBy(ResourceLocation driverId) {
        return allowAnyDriver || allowedDrivers.contains(driverId);
    }
}