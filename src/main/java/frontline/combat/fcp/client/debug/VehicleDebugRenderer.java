package frontline.combat.fcp.client.debug;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frontline.combat.fcp.FCP;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * VehicleDebugRenderer — in-world debug overlay, shared across ALL vehicles.
 *
 * Press the toggle key (default F6) while in game. For every loaded vehicle within
 * range it draws, all from the vehicle's real SBW data (nothing approximated):
 *   - cyan crosses at the vehicle's defined terrainCompat sample points, each with a
 *     ray down to the detected ground (green)
 *   - a red cross at the barrel pivot, with an orange ray along the barrel
 *   - a yellow cross at the turret pivot, with a ray along the turret facing
 *   - purple crosses at each weapon's shoot position (where projectiles spawn)
 *
 * Self-registering (Forge @EventBusSubscriber), so nothing else needs wiring. It is
 * client-only and reads existing SBW accessors, so it works for any VehicleEntity.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VehicleDebugRenderer {

    /** Toggle this with the key; defaults off. */
    public static boolean enabled = false;

    /** Only draw for vehicles within this distance of the camera (blocks). */
    private static final double RANGE = 96.0;

    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.fcp.vehicle_debug",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            "key.categories.fcp");

    private VehicleDebugRenderer() {
    }

    // ── input ──────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (TOGGLE_KEY.consumeClick()) {
            enabled = !enabled;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "[FCP] Vehicle debug overlay " + (enabled ? "ON" : "OFF")), true);
            }
        }
    }

    // ── render ─────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (!enabled) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float pt = event.getPartialTick();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cam = camera.getPosition();

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z); // world space -> camera-relative

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof VehicleEntity vehicle)) continue;
            if (e.position().distanceToSqr(cam) > RANGE * RANGE) continue;

            drawTerrainPoints(mc, vehicle, pt, pose, lines);
            drawBarrel(vehicle, pt, pose, lines);
            drawTurret(vehicle, pt, pose, lines);
            drawShootPositions(vehicle, pt, pose, lines);
        }

        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

    // ── feature draws (each guarded so one odd vehicle can't break the overlay) ──

    private static void drawTerrainPoints(Minecraft mc, VehicleEntity v, float pt,
                                          PoseStack pose, VertexConsumer vc) {
        try {
            // The ACTUAL points SBW samples on your jar: the vehicle's defined
            // terrainCompat list (local coords), transformed to world by the wheels
            // transform — exactly what terrainCompact uses. So the count and layout
            // match the vehicle data (e.g. MATV = 6 points), not an approximation.
            List<Vec3> points = v.computed().getTerrainCompat();
            if (points == null || points.isEmpty()) return;

            // Vehicle's terrainCompat list, placed flat (yaw-only) exactly like SBW samples
            // them. Count/layout match the vehicle data (e.g. MATV = 6 points).
            Matrix4d transform = v.getWheelsTransform(pt);
            for (Vec3 local : points) {
                Vector4d w = v.transformPosition(transform, local.x, local.y, local.z);
                Vec3 p = new Vec3(w.x, w.y, w.z);
                cross(pose, vc, p, 0.08, 0f, 1f, 1f, 1f); // cyan sample point

                // ray down to the detected ground, like terrainCompact's clip
                Vec3 down = p.add(0.0, -20.0, 0.0);
                HitResult hit = mc.level.clip(new ClipContext(
                        p, down, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, v));
                if (hit.getType() != HitResult.Type.MISS) {
                    Vec3 g = hit.getLocation();
                    line(pose, vc, p.x, p.y, p.z, g.x, g.y, g.z, 0f, 0.55f, 0.55f, 1f);
                    cross(pose, vc, g, 0.12, 0f, 1f, 0f, 1f); // green ground hit
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void drawBarrel(VehicleEntity v, float pt, PoseStack pose, VertexConsumer vc) {
        try {
            // Real barrel transform from SBW (VehicleVecUtils), not an estimate.
            Matrix4d bt = v.getBarrelTransform(pt);
            Vector4d bp = v.transformPosition(bt, 0.0, 0.0, 0.0);
            Vec3 pos = new Vec3(bp.x, bp.y, bp.z);
            cross(pose, vc, pos, 0.10, 1f, 0f, 0f, 1f); // red barrel origin

            Vec3 dir = v.getBarrelVector(pt);
            if (dir.lengthSqr() > 1.0e-6) {
                Vec3 tip = pos.add(dir.normalize().scale(3.0));
                line(pose, vc, pos.x, pos.y, pos.z, tip.x, tip.y, tip.z, 1f, 0.6f, 0f, 1f); // orange
            }
        } catch (Throwable ignored) {
        }
    }

    private static void drawTurret(VehicleEntity v, float pt, PoseStack pose, VertexConsumer vc) {
        try {
            // Real turret transform from SBW (VehicleVecUtils), not an estimate.
            Matrix4d tt = v.getTurretTransform(pt);
            Vector4d tp = v.transformPosition(tt, 0.0, 0.0, 0.0);
            Vec3 pos = new Vec3(tp.x, tp.y, tp.z);
            cross(pose, vc, pos, 0.12, 1f, 1f, 0f, 1f); // yellow turret origin

            Vec3 dir = v.getTurretVector(pt);
            if (dir.lengthSqr() > 1.0e-6) {
                Vec3 tip = pos.add(dir.normalize().scale(2.0));
                line(pose, vc, pos.x, pos.y, pos.z, tip.x, tip.y, tip.z, 0.7f, 0.7f, 0f, 1f);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void drawShootPositions(VehicleEntity v, float pt, PoseStack pose, VertexConsumer vc) {
        try {
            // Real muzzle/fire positions: one per weapon, pulled from the vehicle's gun
            // data via getShootPos (the same point projectiles spawn from). Not guessed.
            for (String weaponName : v.getGunDataMap().keySet()) {
                Vec3 sp = v.getShootPos(weaponName, pt);
                cross(pose, vc, sp, 0.11, 0.7f, 0.2f, 1f, 1f); // purple shoot position
            }
        } catch (Throwable ignored) {
        }
    }

    // ── primitives ───────────────────────────────────────────────────────────────

    private static void cross(PoseStack pose, VertexConsumer vc, Vec3 p, double s,
                              float r, float g, float b, float a) {
        line(pose, vc, p.x - s, p.y, p.z, p.x + s, p.y, p.z, r, g, b, a);
        line(pose, vc, p.x, p.y - s, p.z, p.x, p.y + s, p.z, r, g, b, a);
        line(pose, vc, p.x, p.y, p.z - s, p.x, p.y, p.z + s, r, g, b, a);
    }

    private static void line(PoseStack pose, VertexConsumer vc,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             float r, float g, float b, float a) {
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        float nx = (float) (x2 - x1), ny = (float) (y2 - y1), nz = (float) (z2 - z1);
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0e-6f) return;
        nx /= len; ny /= len; nz /= len;
        vc.vertex(m, (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(n, nx, ny, nz).endVertex();
    }

    // ── keybind registration (mod bus) ───────────────────────────────────────────

    @Mod.EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class KeyReg {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(TOGGLE_KEY);
        }
    }
}