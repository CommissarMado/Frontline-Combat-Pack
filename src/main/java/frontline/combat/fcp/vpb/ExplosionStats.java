package frontline.combat.fcp.compat.vpb;

public final class ExplosionStats {
    public final float damage;
    public final float radius;
    public final int fireTime;
    public final boolean destroyBlocks;

    public ExplosionStats(float damage, float radius, int fireTime, boolean destroyBlocks) {
        this.damage = damage;
        this.radius = radius;
        this.fireTime = fireTime;
        this.destroyBlocks = destroyBlocks;
    }

    @Override
    public String toString() {
        return "ExplosionStats{damage=" + damage + ", radius=" + radius
                + ", fireTime=" + fireTime + ", destroyBlocks=" + destroyBlocks + '}';
    }
}
