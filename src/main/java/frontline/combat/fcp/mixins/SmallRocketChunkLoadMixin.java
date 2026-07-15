package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile;
import com.atsuishio.superbwarfare.entity.projectile.SmallRocketEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Lets small rockets keep their chunks loaded in flight - but ONLY the ones fired from
 * an FCP launcher that actually needs it.
 *
 * WHY THIS TARGETS FastThrowableProjectile AND NOT SmallRocketEntity:
 * forceLoadChunk() is declared on FastThrowableProjectile and returns false. Of its
 * subclasses only CannonShellEntity, MediumRocketEntity, MissileProjectile and
 * MortarShellEntity override it. SmallRocketEntity does NOT - it merely inherits it.
 * Mixin injects into the target class's own bytecode, so @Mixin(SmallRocketEntity.class)
 * + @Inject(method = "forceLoadChunk") finds no such method and quietly does nothing.
 * The declaring class is the only valid target.
 *
 * Scoping is therefore done at runtime instead of by mixin target:
 *   - must be a SmallRocketEntity (leaves bullets, shells, missiles etc. untouched)
 *   - the shooter must have been riding a launcher in the set below
 * Everything else falls straight through to SBW's default. Chunk forcing is not free,
 * and an RPG fired across a street has no business paying for it.
 *
 * The answer is latched on the first tick the owner resolves, so the shot still completes
 * if the gunner bails out mid-flight - otherwise getVehicle() would go null and the rocket
 * would strand itself in an unloaded chunk.
 */
@Mixin(value = FastThrowableProjectile.class, remap = false)
public abstract class SmallRocketChunkLoadMixin {

    /** Launchers whose small rockets fly far enough to outrun loaded chunks. */
    @Unique
    private static final Set<String> FCP_CHUNK_LOADING_LAUNCHERS = Set.of(
            "fcp:toyota_hilux_rocket_pod"
    );

    @Unique
    private boolean fcp$forceChunk = false;

    @Unique
    private boolean fcp$resolved = false;

    @Inject(method = "forceLoadChunk", at = @At("HEAD"), cancellable = true, remap = false)
    private void fcp$forceLoadChunk(CallbackInfoReturnable<Boolean> cir) {
        if (!this.fcp$resolved) {
            // Only small rockets are our business; everything else keeps SBW's behaviour.
            if (!((Object) this instanceof SmallRocketEntity rocket)) {
                this.fcp$resolved = true;
                return;
            }

            Entity owner = rocket.getOwner();
            // Owner may not be wired up on the very first call. Retry next tick rather
            // than latching a false negative - this runs every tick anyway.
            if (owner == null) return;

            this.fcp$resolved = true;

            Entity launcher = owner.getVehicle();
            if (launcher != null) {
                String id = EntityType.getKey(launcher.getType()).toString();
                this.fcp$forceChunk = FCP_CHUNK_LOADING_LAUNCHERS.contains(id);
            }
        }

        // Only ever override to TRUE. Never force false, so SBW's default path is
        // genuinely untouched for every other projectile in the game.
        if (this.fcp$forceChunk) {
            cir.setReturnValue(true);
        }
    }
}