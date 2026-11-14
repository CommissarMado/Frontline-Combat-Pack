package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.config.server.ProjectileConfig;
import com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile;
import com.atsuishio.superbwarfare.entity.projectile.MortarShellEntity;
import frontline.combat.fcp.FcpWorldSettings;
import frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Artillery, rocket artillery and mortar rounds always keep their chunks loaded in flight, whatever SBW's server config
 * says (user, 2026-11-03: long shots froze or vanished at the edge of loaded chunks).
 *
 * SBW's FastThrowableProjectile.tick() loads the chunks a round is in and is about to enter only when
 * forceLoadChunk() (true for cannon shells, medium rockets, missiles and mortar shells; the Hilux rocket pod's small
 * rockets via SmallRocketChunkLoadMixin) AND the ProjectileConfig.PROJECTILE_CHUNK_LOADING setting are on, and only
 * while the round is at or below the build limit - high artillery arcs lose their chunks at the top. For our rounds both
 * checks are bypassed; everything else keeps SBW's behaviour.
 *
 * "Ours" = any mortar shell (vehicle or emplaced), the HIMARS GMLRS, and anything fired from a vehicle in
 * FcpWorldSettings.ARTILLERY (M109, Msta, 2S31, the mortar carriers, Grad, the Hilux rocket pod). The answer is latched
 * once the owner resolves, so a round keeps its chunks if the gunner leaves the vehicle mid-flight.
 */
@Mixin(FastThrowableProjectile.class)
public abstract class ArtilleryChunkLoadMixin {

    @Unique
    private boolean fcp$artilleryResolved = false;

    @Unique
    private boolean fcp$artillery = false;

    @Unique
    private boolean fcp$isArtilleryRound() {
        if (fcp$artilleryResolved) return fcp$artillery;
        Object self = this;
        if (self instanceof MortarShellEntity || self instanceof HimarsMissileEntity) {
            fcp$artilleryResolved = true;
            return fcp$artillery = true;
        }
        Entity owner = ((FastThrowableProjectile) self).getOwner();
        if (owner == null) return false; // owner not wired up yet: ask again next tick
        fcp$artilleryResolved = true;
        Entity vehicle = owner.getVehicle();
        fcp$artillery = vehicle != null && FcpWorldSettings.ARTILLERY.contains(EntityType.getKey(vehicle.getType()).toString());
        return fcp$artillery;
    }

    /** SBW's PROJECTILE_CHUNK_LOADING switch: always on for artillery rounds. */
    @Redirect(method = "tick", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/neoforged/neoforge/common/ModConfigSpec$BooleanValue;get()Ljava/lang/Object;"))
    private Object fcp$artilleryChunkLoading(ModConfigSpec.BooleanValue value) {
        if (value == ProjectileConfig.PROJECTILE_CHUNK_LOADING && fcp$isArtilleryRound()) return Boolean.TRUE;
        return value.get();
    }

    /** The "only at or below the build limit" check: no limit for artillery rounds (their arcs go higher). */
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getMaxBuildHeight()I"))
    private int fcp$artilleryChunkHeight(ServerLevel level) {
        return fcp$isArtilleryRound() ? Integer.MAX_VALUE : level.getMaxBuildHeight();
    }
}
