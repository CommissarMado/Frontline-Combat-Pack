package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile;
import com.atsuishio.superbwarfare.entity.projectile.SmallRocketEntity;
import frontline.combat.fcp.entity.vehicle.IndirectFireVehicleBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Force-load chunks for small rockets fired as FCP rocket artillery (BVR), matching
 * SBW PLZ05 / cannon-shell behavior ({@code forceLoadChunk() == true}).
 *
 * <p>SBW already force-loads for {@code CannonShellEntity}, {@code MediumRocketEntity}
 * (Ural Grad projectile), {@code MortarShellEntity}, and missiles. {@code SmallRocketEntity}
 * inherits {@code FastThrowableProjectile.forceLoadChunk() == false}, so aircraft RPGs and
 * street-range rockets stay cheap — only FCP artillery launchers opt in here.
 *
 * <p>WHY THIS TARGETS FastThrowableProjectile AND NOT SmallRocketEntity:
 * {@code forceLoadChunk()} is declared on {@code FastThrowableProjectile}. Mixin injects into
 * the declaring class's bytecode; targeting the subclass finds no method and does nothing.
 *
 * <p>Default rule: any small rocket whose shooter is riding (or is) an
 * {@link IndirectFireVehicleBase} force-loads — covers Toyota rocket pod, future MLRS on that
 * base, and fire-control shots. An explicit ID set remains for non-indirect launchers that
 * still need BVR (if any). Grad's medium rockets already force-load in SBW and never hit this path.
 *
 * <p>The answer is latched on the first tick the owner resolves, so the shot still completes
 * if the gunner bails mid-flight (otherwise {@code getVehicle()} goes null and the rocket
 * would strand in an unloaded chunk).
 */
@Mixin(value = FastThrowableProjectile.class, remap = false)
public abstract class SmallRocketChunkLoadMixin {

    /**
     * Extra launcher IDs that are not {@link IndirectFireVehicleBase} but still fire
     * small rockets at artillery ranges. Keep empty unless a non-indirect MLRS appears.
     */
    @Unique
    private static final Set<String> FCP_CHUNK_LOADING_LAUNCHERS = Set.of(
            // Rocket artillery on IndirectFireVehicleBase is covered by instanceof below.
            // Explicit IDs only for edge cases:
            // "fcp:some_other_mlrs"
    );

    @Unique
    private boolean fcp$forceChunk = false;

    @Unique
    private boolean fcp$resolved = false;

    @Inject(method = "forceLoadChunk", at = @At("HEAD"), cancellable = true, remap = false)
    private void fcp$forceLoadChunk(CallbackInfoReturnable<Boolean> cir) {
        if (!this.fcp$resolved) {
            // Only small rockets are our business; shells / medium rockets / missiles keep SBW defaults.
            if (!((Object) this instanceof SmallRocketEntity rocket)) {
                this.fcp$resolved = true;
                return;
            }

            Entity owner = rocket.getOwner();
            // Owner may not be wired on the very first call. Retry next tick rather than
            // latching a false negative — this runs every tick anyway.
            if (owner == null) {
                return;
            }

            this.fcp$resolved = true;
            this.fcp$forceChunk = fcp$shouldForceLoadFromOwner(owner);
        }

        // Only ever override to TRUE. Never force false, so SBW's default path is
        // genuinely untouched for every other projectile.
        if (this.fcp$forceChunk) {
            cir.setReturnValue(true);
        }
    }

    /**
     * True when the shot was fired from FCP rocket artillery: gunner on an indirect-fire
     * vehicle, or the vehicle entity itself as owner, or an explicit launcher ID.
     */
    @Unique
    private static boolean fcp$shouldForceLoadFromOwner(Entity owner) {
        if (owner instanceof IndirectFireVehicleBase) {
            return true;
        }

        Entity vehicle = owner.getVehicle();
        if (vehicle instanceof IndirectFireVehicleBase) {
            return true;
        }

        Entity launcher = vehicle != null ? vehicle : owner;
        String id = EntityType.getKey(launcher.getType()).toString();
        return FCP_CHUNK_LOADING_LAUNCHERS.contains(id);
    }
}
