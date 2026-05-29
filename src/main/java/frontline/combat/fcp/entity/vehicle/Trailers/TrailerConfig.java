package frontline.combat.fcp.entity.vehicle.Trailers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * TrailerConfig — defines the trailer's own geometry.
 * Loaded from: data/<namespace>/trailers/<id>.json
 *
 * The hitch point is no longer defined here — it comes from the
 * towing vehicle's TowableConfig (data/fcp/towable_vehicles/<id>.json).
 *
 * Example (data/fcp/trailers/example_trailer.json):
 * {
 *   "trailer_length": 4.0,
 *   "max_health": 300.0,
 *   "width": 2.5,
 *   "height": 1.8,
 *   "terrain_follow": true,
 *   "seats": [
 *     { "offset_x": 0.0, "offset_y": 1.2, "offset_z": 0.5 }
 *   ]
 * }
 *
 * trailer_length = distance in blocks from front hitch connection to rear.
 *   The entity center is placed at the midpoint along this length.
 */
public record TrailerConfig(
        double trailerLength,
        float maxHealth,
        float width,
        float height,
        boolean terrainFollow,
        List<SeatConfig> seats
) {
    public static final Codec<TrailerConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("trailer_length").forGetter(TrailerConfig::trailerLength),
            Codec.FLOAT.optionalFieldOf("max_health", 200.0f).forGetter(TrailerConfig::maxHealth),
            Codec.FLOAT.optionalFieldOf("width", 2.5f).forGetter(TrailerConfig::width),
            Codec.FLOAT.optionalFieldOf("height", 1.8f).forGetter(TrailerConfig::height),
            Codec.BOOL.optionalFieldOf("terrain_follow", true).forGetter(TrailerConfig::terrainFollow),
            SeatConfig.CODEC.listOf().optionalFieldOf("seats", List.of()).forGetter(TrailerConfig::seats)
    ).apply(inst, TrailerConfig::new));

    /**
     * A passenger seat in trailer-local space.
     * offset_z: +toward front (hitch end), -toward rear
     */
    public record SeatConfig(
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        public static final Codec<SeatConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.DOUBLE.optionalFieldOf("offset_x", 0.0).forGetter(SeatConfig::offsetX),
                Codec.DOUBLE.optionalFieldOf("offset_y", 1.0).forGetter(SeatConfig::offsetY),
                Codec.DOUBLE.optionalFieldOf("offset_z", 0.0).forGetter(SeatConfig::offsetZ)
        ).apply(inst, SeatConfig::new));
    }
}