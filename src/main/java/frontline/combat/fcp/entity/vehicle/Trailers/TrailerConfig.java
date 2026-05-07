package frontline.combat.fcp.entity.vehicle.Trailers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * TrailerConfig — loaded from data/<namespace>/trailers/<id>.json
 *
 * Minimum required JSON:
 * {
 *   "hitch": {
 *     "offset_z": -0.5
 *   },
 *   "trailer_length": 4.0
 * }
 *
 * Full example:
 * {
 *   "hitch": {
 *     "offset_x":  0.0,
 *     "offset_y":  0.5,
 *     "offset_z": -0.5
 *   },
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
 * Hitch offset is in the towing vehicle's local coordinate space:
 *   offset_x = lateral      (+right of tower, -left)
 *   offset_y = vertical     (+above tower origin)
 *   offset_z = longitudinal (-behind tower, +in front)
 *
 * trailer_length = blocks from hitch pin to rear axle.
 * Entity center is placed at the midpoint between hitch and axle.
 */
public record TrailerConfig(
        HitchConfig hitch,
        double trailerLength,
        float maxHealth,
        float width,
        float height,
        boolean terrainFollow,
        List<SeatConfig> seats
) {
    public static final Codec<TrailerConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            HitchConfig.CODEC.fieldOf("hitch").forGetter(TrailerConfig::hitch),
            Codec.DOUBLE.fieldOf("trailer_length").forGetter(TrailerConfig::trailerLength),
            Codec.FLOAT.optionalFieldOf("max_health", 200.0f).forGetter(TrailerConfig::maxHealth),
            Codec.FLOAT.optionalFieldOf("width", 2.5f).forGetter(TrailerConfig::width),
            Codec.FLOAT.optionalFieldOf("height", 1.8f).forGetter(TrailerConfig::height),
            Codec.BOOL.optionalFieldOf("terrain_follow", true).forGetter(TrailerConfig::terrainFollow),
            SeatConfig.CODEC.listOf().optionalFieldOf("seats", List.of()).forGetter(TrailerConfig::seats)
    ).apply(inst, TrailerConfig::new));

    /**
     * The pin point on the towing vehicle where this trailer attaches.
     * All offsets are in the towing vehicle's local coordinate space.
     */
    public record HitchConfig(
            double offsetX,
            double offsetY,
            double offsetZ,
            float smoothing
    ) {
        public static final Codec<HitchConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.DOUBLE.optionalFieldOf("offset_x", 0.0).forGetter(HitchConfig::offsetX),
                Codec.DOUBLE.optionalFieldOf("offset_y", 0.5).forGetter(HitchConfig::offsetY),
                Codec.DOUBLE.fieldOf("offset_z").forGetter(HitchConfig::offsetZ),
                Codec.FLOAT.optionalFieldOf("smoothing", 0.3f).forGetter(HitchConfig::smoothing)
        ).apply(inst, HitchConfig::new));
    }

    /**
     * A single passenger seat on the trailer.
     * Offset is in the trailer's local coordinate space (rotated with trailer yaw).
     *   offset_x = lateral  (+right, -left)
     *   offset_y = vertical (+up)
     *   offset_z = forward  (+toward hitch end, -toward axle end)
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
