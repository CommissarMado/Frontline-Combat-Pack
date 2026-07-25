package frontline.combat.fcp.entity.vehicle.Trailers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Hitch point on a towing vehicle, in its local space (x right, y up, z forward — rear
 * hitches use negative z). Read from data/<ns>/trailer_driver/<entity_id>.json; a vehicle
 * without one cannot tow.
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