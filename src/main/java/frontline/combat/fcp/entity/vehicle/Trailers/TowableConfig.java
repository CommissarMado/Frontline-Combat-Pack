package frontline.combat.fcp.entity.vehicle.Trailers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * TowableConfig — defines the hitch point on a towing vehicle.
 *
 * One JSON file per towable vehicle, placed at:
 *   data/fcp/towable_vehicles/<vehicle_id>.json
 *
 * Example for the Kamaz (data/fcp/towable_vehicles/kamaz.json):
 * {
 *   "hitch_x":  0.0,
 *   "hitch_y":  0.8,
 *   "hitch_z": -2.5,
 *   "smoothing": 0.25
 * }
 *
 * hitch_x = lateral offset from vehicle center (+right, -left)
 * hitch_y = vertical offset from vehicle origin (+up)
 * hitch_z = longitudinal offset from vehicle center (-behind, +front)
 *           Negative = rear of vehicle. Set this to where the tow bar is.
 *
 * smoothing = how quickly the trailer rotates to face the hitch point.
 *   1.0 = instant (rigid bar)
 *   0.25 = default (natural lag through corners)
 *   0.1 = very loose
 */
public record TowableConfig(
        double hitchX,
        double hitchY,
        double hitchZ,
        float smoothing
) {
    public static final Codec<TowableConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.optionalFieldOf("hitch_x", 0.0).forGetter(TowableConfig::hitchX),
            Codec.DOUBLE.optionalFieldOf("hitch_y", 0.5).forGetter(TowableConfig::hitchY),
            Codec.DOUBLE.fieldOf("hitch_z").forGetter(TowableConfig::hitchZ),
            Codec.FLOAT.optionalFieldOf("smoothing", 0.25f).forGetter(TowableConfig::smoothing)
    ).apply(inst, TowableConfig::new));
}
