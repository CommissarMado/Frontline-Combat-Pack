package frontline.combat.fcp.entity.vehicle.Trailers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * TrailerDriverData — the hitch point on a TOWING (driver) vehicle.
 *
 * One JSON file per towing vehicle, placed at:
 *   data/<namespace>/trailer_driver/<entity_id>.json
 * where <entity_id> is the vehicle's registry name (e.g. "kamaz").
 *
 * The hitch point is expressed in the driver vehicle's LOCAL space:
 *   hitch_x = lateral offset  (+right, -left)
 *   hitch_y = vertical offset (+up)
 *   hitch_z = longitudinal    (+forward, -backward) — usually negative (rear)
 *
 * Example (data/fcp/trailer_driver/kamaz.json):
 * {
 *   "hitch_x":  0.0,
 *   "hitch_y":  0.7,
 *   "hitch_z": -3.6
 * }
 *
 * A vehicle WITHOUT one of these files simply cannot tow anything.
 */
public record TrailerDriverData(
        double hitchX,
        double hitchY,
        double hitchZ
) {
    public static final Codec<TrailerDriverData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.optionalFieldOf("hitch_x", 0.0).forGetter(TrailerDriverData::hitchX),
            Codec.DOUBLE.optionalFieldOf("hitch_y", 0.5).forGetter(TrailerDriverData::hitchY),
            Codec.DOUBLE.fieldOf("hitch_z").forGetter(TrailerDriverData::hitchZ)
    ).apply(inst, TrailerDriverData::new));
}