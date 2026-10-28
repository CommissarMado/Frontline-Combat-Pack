package frontline.combat.fcp.balance;

import com.atsuishio.superbwarfare.entity.mixin.OBBHitter;
import com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModTags;
import com.atsuishio.superbwarfare.tools.OBB;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.RoofCover;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Squad's vehicle damage model on top of SBW (data: data/fcp/fcp_armor/&lt;vehicle&gt;.json).
 * <p>
 * Every round fired from a listed vehicle is tagged at spawn with its Squad damage type and armour
 * penetration (mm), from the vehicle's weapon table keyed by gun name ("Cannon", or "Cannon#1" for its
 * second ammo type). When a tagged round - or small-arms fire, or an explosion - hits a listed vehicle,
 * SBW's damage modifiers are replaced by Squad's:
 * <ol>
 *   <li>Hits - direct and blasts alike - check penetration against the armour of the face that was hit (front / side / rear /
 *       top of the hull, or front / side of the turret when the turret hitbox was hit). A kinetic or
 *       small-arms round that doesn't penetrate does nothing; a HEAT round that doesn't penetrate does
 *       {@link #HEAT_NON_PEN} of its damage.</li>
 *   <li>The vehicle's Squad modifier for the damage type is applied (e.g. IFV hull: kinetic 0.1,
 *       HEAT 0.3, explosive 1; armour is immune to small arms).</li>
 * </ol>
 * Untagged blasts count as {@link #FRAG_PEN} mm fragments (grenades, launchers), mines as {@link #MINE_PEN}.
 * Anything else (rounds from
 * vehicles without a table, collisions, fire...) keeps SBW's own modifiers.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class VehicleArmor {

    public static final String TAG = "fcp_round";
    /** Damage left from a HEAT/HAT hit that fails to penetrate (spall/overpressure). */
    private static final float HEAT_NON_PEN = 0.1f;
    /** Small-arms penetration (mm) when the shooter has no weapon table (infantry rifles, MGs). */
    private static final float SMALL_ARMS_PEN = 7f;
    /** Penetration (mm) of untagged blasts: grenades and launchers (frag), mines, a vehicle cooking off. */
    private static final float FRAG_PEN = 5f, MINE_PEN = 150f, COOK_OFF_PEN = 50f;
    /** Incoming hits steeper than this (sine of the angle below horizontal) strike the top armour. */
    private static final double TOP_HIT = 0.6;
    /** Crater radius share for fragmentation rounds: they're meant to shred, not dig. */
    private static final float FRAG_BLOCK = 0.35f;

    public record Round(String type, float pen) {}

    /** Squad loadout of one weapon / ammo type: {@code mags} magazines of {@code rounds} each. */
    public record Loadout(int mags, int rounds) {
        public int total() {
            return mags * rounds;
        }
    }

    /**
     * {@code model} false: the vehicle only tags its rounds (emplacements); its own damage stays SBW's.
     * {@code ammo}: Squad loadout per weapon key, used by the rearm station.
     */
    public record Profile(Map<String, Float> armor, Map<String, Float> modifiers, Map<String, Round> weapons, boolean model,
                          Map<String, Loadout> ammo) {
        float armor(String face) {
            return armor.getOrDefault(face, armor.getOrDefault("side", 0f));
        }

        float modifier(String type) {
            return modifiers.getOrDefault(type, 1f);
        }
    }

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static Map<String, Profile> profiles = Map.of();

    private VehicleArmor() {
    }

    @Nullable
    public static Profile profile(Entity vehicle) {
        return profiles.get(EntityType.getKey(vehicle.getType()).toString());
    }

    // ---- data ---------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(new Loader());
    }

    private static final class Loader extends SimpleJsonResourceReloadListener {
        Loader() {
            super(new com.google.gson.Gson(), "fcp_armor");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, Profile> out = new HashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> e : files.entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                Map<String, Float> armor = floats(o.getAsJsonObject("armor"));
                Map<String, Float> mods = floats(o.getAsJsonObject("modifiers"));
                Map<String, Round> weapons = new HashMap<>();
                if (o.has("weapons")) {
                    for (Map.Entry<String, JsonElement> w : o.getAsJsonObject("weapons").entrySet()) {
                        JsonObject r = w.getValue().getAsJsonObject();
                        weapons.put(w.getKey(), new Round(r.get("type").getAsString(), r.has("pen") ? r.get("pen").getAsFloat() : 0f));
                    }
                }
                String id = e.getKey().getNamespace() + ":" + e.getKey().getPath();
                Map<String, Loadout> ammo = new HashMap<>();
                if (o.has("ammo")) {
                    for (Map.Entry<String, JsonElement> w : o.getAsJsonObject("ammo").entrySet()) {
                        var arr = w.getValue().getAsJsonArray();
                        ammo.put(w.getKey(), new Loadout(arr.get(0).getAsInt(), arr.get(1).getAsInt()));
                    }
                }
                out.put(id, new Profile(armor, mods, weapons, !o.has("model") || o.get("model").getAsBoolean(), ammo));
            }
            profiles = out;
            LOGGER.info("[FCP] Loaded {} vehicle armour profiles", out.size());
        }

        private static Map<String, Float> floats(@Nullable JsonObject o) {
            Map<String, Float> m = new HashMap<>();
            if (o != null) for (Map.Entry<String, JsonElement> e : o.entrySet()) m.put(e.getKey(), e.getValue().getAsFloat());
            return m;
        }
    }

    // ---- tagging rounds -------------------------------------------------------------------------

    /**
     * Rounds tagged by projectile type when no vehicle profile tags them (shoulder-fired launchers):
     * anti-air missiles must get through every helicopter's armour.
     */
    private static final Round AIR_DEFENCE = new Round("explosive", 100f);
    /**
     * AA missile damage (same as the vehicle-mounted ones' configs). A hit lands about 80 on a helicopter
     * (x1.5 explosive): a Little Bird (73) dies, a medium helicopter (97) is left smoking just above the
     * 10% where SBW sets it on fire, a heavy one (145) at about half.
     */
    private static final float AA_HIT = 13f, AA_BLAST = 40f, AA_RADIUS = 8f;
    private static final Map<String, Round> BY_PROJECTILE = Map.of(
            "superbwarfare:fim_92_missile", AIR_DEFENCE,
            "superbwarfare:igla_9k38_missile", AIR_DEFENCE,
            "superbwarfare:ru_9m336_missile", AIR_DEFENCE,
            "superbwarfare:ru_9m100_missile", AIR_DEFENCE);

    /** Tags a round fired from a listed vehicle with its weapon's Squad type and penetration. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Projectile round)) return;
        if (round.getPersistentData().contains(TAG)) return;
        Round r = vehicleRound(round);
        if (r == null) {
            r = BY_PROJECTILE.get(EntityType.getKey(round.getType()).toString());
            // SBW's own launchers carry their own damage: bring it to the vehicle-mounted AA's numbers.
            if (r == AIR_DEFENCE && round instanceof FastThrowableProjectile missile) {
                missile.setDamage(AA_HIT);
                missile.setExplosionDamage(AA_BLAST);
                missile.setExplosionRadius(AA_RADIUS);
            }
        }
        if (r == null) return;
        CompoundTag t = new CompoundTag();
        t.putString("type", r.type());
        t.putFloat("pen", r.pen());
        round.getPersistentData().put(TAG, t);
    }

    /** The Squad round of the weapon that fired {@code round}, if its shooter sits in a listed vehicle. */
    @Nullable
    private static Round vehicleRound(Projectile round) {
        Entity shooter = round.getOwner();
        if (shooter == null || !(shooter.getVehicle() instanceof VehicleEntity vehicle)) return null;
        Profile p = profile(vehicle);
        if (p == null) return null;
        int seat = vehicle.getSeatIndex(shooter);
        String gun = vehicle.getGunName(seat);
        if (gun == null) return null;
        var data = vehicle.getGunData(seat);
        int ammo = data == null ? 0 : data.selectedAmmoType.get();
        return ammo > 0 ? p.weapons().getOrDefault(gun + "#" + ammo, p.weapons().get(gun)) : p.weapons().get(gun);
    }

    /**
     * Share of SBW's crater radius a round's explosion keeps: its tag's "block" value, else
     * {@link #FRAG_BLOCK} for fragmentation rounds and 1 for everything else.
     */
    public static float blockScale(@Nullable Entity direct) {
        if (direct == null || !direct.getPersistentData().contains(TAG)) return 1f;
        CompoundTag tag = direct.getPersistentData().getCompound(TAG);
        if (tag.contains("block")) return tag.getFloat("block");
        return tag.getString("type").equals("frag") ? FRAG_BLOCK : 1f;
    }

    // ---- damage -----------------------------------------------------------------------------------

    /**
     * The damage a vehicle takes: {@link #squadDamage}, then any roof cover ({@link RoofCover}, e.g. the T-90M's cope
     * cage) for hits from above - applied with or without the armour model.
     */
    public static float damage(VehicleEntity vehicle, DamageSource source, float raw, float sbwDamage) {
        float damage = squadDamage(vehicle, source, raw, sbwDamage);
        if (vehicle instanceof RoofCover cover && damage > 0 && -incoming(vehicle, source, source.getDirectEntity()).y > TOP_HIT) {
            damage *= cover.roofDamageMultiplier();
        }
        return damage;
    }

    /**
     * Squad damage for a hit on a listed vehicle, or {@code sbwDamage} when the hit isn't one we model.
     * {@code raw} is the damage before SBW's modifiers.
     */
    private static float squadDamage(VehicleEntity vehicle, DamageSource source, float raw, float sbwDamage) {
        // Switched off per world (F8) for packs that expect SBW's own damage model.
        if (!frontline.combat.fcp.FcpWorldSettings.current().armorModel()) return sbwDamage;
        Profile p = profile(vehicle);
        if (p == null || !p.model()) return sbwDamage;
        Entity direct = source.getDirectEntity();
        CompoundTag tag = direct != null && direct.getPersistentData().contains(TAG) ? direct.getPersistentData().getCompound(TAG) : null;

        boolean explosion = source.is(DamageTypeTags.IS_EXPLOSION) || source.is(ModDamageTypes.CUSTOM_EXPLOSION)
                || source.is(ModDamageTypes.PROJECTILE_EXPLOSION) || source.is(ModDamageTypes.MINE) || source.is(ModDamageTypes.LUNGE_MINE)
                || source.is(ModDamageTypes.VEHICLE_EXPLOSION);

        String type;
        float pen;
        if (explosion) {
            // Blasts must beat the armour facing them too: a grenade does nothing to a tank.
            if (tag != null) {
                type = tag.getString("type").equals("frag") ? "frag" : "explosive";
                pen = tag.getFloat("pen");
            } else if (source.is(ModDamageTypes.MINE) || source.is(ModDamageTypes.LUNGE_MINE)) {
                type = "explosive";
                pen = MINE_PEN;
            } else if (source.is(ModDamageTypes.VEHICLE_EXPLOSION)) {
                type = "explosive";
                pen = COOK_OFF_PEN;
            } else {
                type = "frag";
                pen = FRAG_PEN;
            }
        } else if (tag != null) {
            type = tag.getString("type");
            pen = tag.getFloat("pen");
        } else if (source.is(ModTags.DamageTypes.GUN_DAMAGE)) {
            type = "small_arms";
            pen = SMALL_ARMS_PEN;
        } else {
            return sbwDamage;
        }

        float mod = p.modifier(type);
        if (mod <= 0) return 0;
        float armor = p.armor(face(vehicle, source, direct));
        if (pen < armor) {
            // Only a direct HEAT hit leaves anything (spall) when it fails to penetrate.
            return !explosion && (type.equals("heat") || type.equals("hat")) ? raw * HEAT_NON_PEN * mod : 0;
        }
        return raw * mod;
    }

    /** Unit direction the hit travelled in (zero if unknown): the round's motion, else from the source to the vehicle. */
    private static Vec3 incoming(VehicleEntity vehicle, DamageSource source, @Nullable Entity direct) {
        Vec3 incoming = direct != null ? direct.getDeltaMovement() : Vec3.ZERO;
        if (incoming.lengthSqr() < 1.0e-6) {
            Vec3 from = source.getSourcePosition();
            incoming = from == null ? Vec3.ZERO : vehicle.position().subtract(from);
        }
        return incoming.lengthSqr() < 1.0e-6 ? Vec3.ZERO : incoming.normalize();
    }

    /** Which armour face the hit struck. */
    private static String face(VehicleEntity vehicle, DamageSource source, @Nullable Entity direct) {
        Vec3 incoming = incoming(vehicle, source, direct);
        if (incoming.lengthSqr() < 1.0e-6) return "side";
        boolean turret = direct instanceof Projectile && OBBHitter.Companion.getInstance(direct).sbw$getCurrentHitPart() == OBB.Part.TURRET;
        if (!turret && -incoming.y > TOP_HIT) return "top";

        float facing = turret ? vehicle.getYRot() - vehicle.getTurretYRot() : vehicle.getYRot();
        // Direction the round came FROM, as a yaw, against the way the hull/turret faces.
        double fromYaw = Math.toDegrees(Math.atan2(incoming.x, -incoming.z));
        double rel = Mth.wrapDegrees(fromYaw - facing);
        double abs = Math.abs(rel);
        String face = abs <= 45 ? "front" : abs >= 135 ? "rear" : "side";
        return turret ? "turret_" + (face.equals("rear") ? "side" : face) : face;
    }
}
