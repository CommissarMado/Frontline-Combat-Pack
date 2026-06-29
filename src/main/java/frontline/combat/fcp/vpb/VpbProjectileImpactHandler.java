package frontline.combat.fcp.compat.vpb;

import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.tools.CustomExplosion;
import com.atsuishio.superbwarfare.tools.DamageHandler;
import com.mojang.logging.LogUtils;
import frontline.combat.fcp.FCP;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;


@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class VpbProjectileImpactHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private VpbProjectileImpactHandler() {
    }

    @SubscribeEvent
    @SuppressWarnings("removal") // setCanceled is deprecated-for-removal but is the only cancel path VPB honours; see below
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!VpbIntegration.isVpbLoaded()) return;

        VpbIntegrationConfig cfg = VpbIntegrationConfig.get();
        if (cfg.projectileWarheads.isEmpty()) return;

        Projectile projectile = event.getProjectile();
        if (projectile == null) return;

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(projectile.getType());
        WarheadStats stats = cfg.projectileWarheads.get(id);
        if (stats == null) {
            if (cfg.debugLogging && id != null && VpbIntegration.VPB_MODID.equals(id.getNamespace())) {
                LOGGER.info("[FCP/VPB] Unmapped pointblank projectile impact: {} (add it to projectileWarheads to replace its impact)", id);
            }
            return;
        }


        event.setCanceled(true);

        Level level = projectile.level();
        HitResult result = event.getRayTraceResult();
        Vec3 pos = result.getLocation();

        if (!level.isClientSide) {
            Entity shooter = projectile.getOwner();

            // 1) Direct hit on the struck entity (entity hits only), through SBW's projectile-hit
            //    damage type so vehicle DamageModifiers/armor apply.
            if (stats.hasDirectHit() && result instanceof EntityHitResult entityHit) {
                Entity target = entityHit.getEntity();
                if (target != null) {
                    DamageHandler.doDamage(
                            target,
                            ModDamageTypes.causeProjectileHitDamage(level.registryAccess(), projectile, shooter),
                            stats.directDamage);
                }
            }

            // 2) Explosion AoE (matches SBW's buildExplosion: attacker = shooter, origin = projectile,
            //    explicit particle tier - the Builder would otherwise default a missing type to MINI).
            if (stats.hasExplosion()) {
                CustomExplosion.Builder builder = new CustomExplosion.Builder(projectile)
                        .attacker(shooter)
                        .damage(stats.explosionDamage)
                        .radius(stats.explosionRadius)
                        .withParticleType(stats.resolveExplosionParticle())
                        .fireTime(stats.fireTime)
                        .position(pos);
                if (!stats.destroyBlocks) {
                    builder.keepBlock();
                }
                builder.explode();
            }

            if (cfg.debugLogging) {
                LOGGER.info("[FCP/VPB] {} -> SBW warhead {} at {}", id, stats, pos);
            }
        }

        // VPB will not have discarded the projectile because we cancelled the impact.
        projectile.discard();
    }
}