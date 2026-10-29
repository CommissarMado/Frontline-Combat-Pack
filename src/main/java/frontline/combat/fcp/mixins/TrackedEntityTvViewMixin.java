package frontline.combat.fcp.mixins;

import frontline.combat.fcp.entity.vehicle.Himars.HimarsTvView;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * HIMARS TV guidance: while a player flies a TV missile, which entities they are sent is judged from the missile
 * (HimarsTvView) - except their own vehicle and its crew, which stay tracked so they're still seated when the view
 * comes home.
 */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class TrackedEntityTvViewMixin {

    @Shadow
    @Final
    Entity entity;

    @Redirect(method = "updatePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 fcp$viewPosition(ServerPlayer player) {
        if (entity.getRootVehicle() == player.getRootVehicle()) return player.position();
        return HimarsTvView.viewOf(player).position();
    }
}
