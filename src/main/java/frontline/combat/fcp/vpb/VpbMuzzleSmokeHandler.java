package frontline.combat.fcp.compat.vpb;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import com.mojang.logging.LogUtils;
import frontline.combat.fcp.FCP;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;


@EventBusSubscriber(modid = FCP.MODID)
public final class VpbMuzzleSmokeHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int DOWNRANGE_SMOKE_DELAY_TICKS = 2;

    private VpbMuzzleSmokeHandler() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!VpbIntegration.isVpbLoaded()) return;

        Level level = event.getLevel();
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        VpbIntegrationConfig cfg = VpbIntegrationConfig.get();
        if (cfg.muzzleSmokeProjectiles.isEmpty()) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Projectile projectile)) return;

        ResourceLocation projId = ForgeRegistries.ENTITY_TYPES.getKey(projectile.getType());
        if (projId == null) return;

        if (!cfg.muzzleSmokeProjectiles.contains(projId)) {
            if (cfg.debugLogging && VpbIntegration.VPB_MODID.equals(projId.getNamespace())) {
                LOGGER.info("[FCP/VPB] pointblank projectile {} spawned (add it to muzzleSmokeProjectiles for RPG smoke)", projId);
            }
            return;
        }

        // 1) Muzzle puff at the shooter (matches RpgItem.shootBullet); fall back to the projectile
        //    spawn position if there is no living shooter.
        Vec3 muzzle;
        if (projectile.getOwner() instanceof LivingEntity shooter) {
            Vec3 look = shooter.getLookAngle();
            muzzle = new Vec3(
                    shooter.getX() + 1.8 * look.x,
                    shooter.getY() + shooter.getBbHeight() - 0.1 + 1.8 * look.y,
                    shooter.getZ() + 1.8 * look.z);
        } else {
            muzzle = projectile.position();
        }
        ParticleTool.sendParticle(serverLevel, ParticleTypes.CLOUD, muzzle.x, muzzle.y, muzzle.z,
                30, 0.4, 0.4, 0.4, 0.005, true);

        // 2) Downrange ignition smoke a few ticks later, at the projectile's position then
        //    (matches RpgRocketStandardEntity.tick() at tickCount == 3).
        Mod.queueServerWork(DOWNRANGE_SMOKE_DELAY_TICKS, () -> {
            if (!projectile.isAlive()) return;
            Vec3 p = projectile.position();
            ParticleTool.sendParticle(serverLevel, ParticleTypes.CLOUD, p.x, p.y, p.z,
                    15, 0.8, 0.8, 0.8, 0.01, true);
            ParticleTool.sendParticle(serverLevel, ParticleTypes.CAMPFIRE_COSY_SMOKE, p.x, p.y, p.z,
                    10, 0.8, 0.8, 0.8, 0.01, true);
        });
    }
}
