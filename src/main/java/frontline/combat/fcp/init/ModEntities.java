package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Hellfire.LockOnHellfireEntity;
import frontline.combat.fcp.entity.projectile.Hellfire.WireGuidedHellfireEntity;
import frontline.combat.fcp.entity.projectile.Malyutka.MalyutkaEntity;
import frontline.combat.fcp.entity.projectile.Sidewinder.SidewinderEntity;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrGLEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrMGEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrRWSEntity;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazEntity;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdArmedEntity;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerM2Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMGSEntity;
import frontline.combat.fcp.entity.vehicle.T72av.T72AVEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxBMPEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxRocketPodEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxSpg9Entity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZDSHKAEntity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralGradEntity;
import frontline.combat.fcp.entity.vehicle.Viper.ViperEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FCP.MODID);

    public static final RegistryObject<EntityType<ToyotaHiluxEntity>> TOYOTA_HILUX = register("toyota_hilux",
            EntityType.Builder.of(ToyotaHiluxEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final RegistryObject<EntityType<ToyotaHiluxRocketPodEntity>> TOYOTA_HILUX_ROCKET_POD = register("toyota_hilux_rocket_pod",
            EntityType.Builder.of(ToyotaHiluxRocketPodEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final RegistryObject<EntityType<ToyotaHiluxBMPEntity>> TOYOTA_HILUX_BMP = register("toyota_hilux_bmp",
            EntityType.Builder.of(ToyotaHiluxBMPEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final RegistryObject<EntityType<ToyotaHiluxSpg9Entity>> TOYOTA_HILUX_SPG9 = register("toyota_hilux_spg9",
            EntityType.Builder.of(ToyotaHiluxSpg9Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final RegistryObject<EntityType<UAZEntity>> UAZ = register("uaz",
            EntityType.Builder.of(UAZEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));

    public static final RegistryObject<EntityType<UAZDSHKAEntity>> UAZ_DSHKA = register("uaz_dshka",
            EntityType.Builder.of(UAZDSHKAEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final RegistryObject<EntityType<StrykerMGSEntity>> STRYKER_MGS = register("stryker_mgs",
            EntityType.Builder.of(StrykerMGSEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final RegistryObject<EntityType<StrykerM2Entity>> STRYKER_M2 = register("stryker_m2",
            EntityType.Builder.of(StrykerM2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final RegistryObject<EntityType<LittlebirdEntity>> LITTLEBIRD = register("littlebird",
            EntityType.Builder.of(LittlebirdEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));

    public static final RegistryObject<EntityType<LittlebirdArmedEntity>> LITTLEBIRD_ARMED = register("littlebird_armed",
            EntityType.Builder.of(LittlebirdArmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));

    public static final RegistryObject<EntityType<BMP1Entity>> BMP1 = register("bmp1",
            EntityType.Builder.of(BMP1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final RegistryObject<EntityType<Lav25Entity>> LAV25 = register("lav25",
            EntityType.Builder.of(Lav25Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final RegistryObject<EntityType<T72AVEntity>> T72AV = register("t72av",
            EntityType.Builder.of(T72AVEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<UralEntity>> URAL = register("ural",
            EntityType.Builder.of(UralEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<UralGradEntity>> URAL_GRAD = register("ural_grad",
            EntityType.Builder.of(UralGradEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<KamazEntity>> KAMAZ = register("kamaz",
            EntityType.Builder.of(KamazEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<ViperEntity>> VIPER = register("viper",
            EntityType.Builder.of(ViperEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<GazTigrEntity>> GAZ_TIGR = register("gaz_tigr",
            EntityType.Builder.of(GazTigrEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<GazTigrRWSEntity>> GAZ_TIGR_RWS = register("gaz_tigr_rws",
            EntityType.Builder.of(GazTigrRWSEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<GazTigrMGEntity>> GAZ_TIGR_MG = register("gaz_tigr_mg",
            EntityType.Builder.of(GazTigrMGEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<GazTigrGLEntity>> GAZ_TIGR_GL = register("gaz_tigr_gl",
            EntityType.Builder.of(GazTigrGLEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    // Projectiles
    public static final RegistryObject<EntityType<LockOnHellfireEntity>> LOCK_ON_HELLFIRE = register("lock_on_hellfire",
            EntityType.Builder.<LockOnHellfireEntity>of(LockOnHellfireEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    public static final RegistryObject<EntityType<WireGuidedHellfireEntity>> WIRE_GUIDED_HELLFIRE = register("wire_guided_hellfire",
            EntityType.Builder.<WireGuidedHellfireEntity>of(WireGuidedHellfireEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    public static final RegistryObject<EntityType<SidewinderEntity>> SIDEWINDER = register("sidewinder",
            EntityType.Builder.<SidewinderEntity>of(SidewinderEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    public static final RegistryObject<EntityType<MalyutkaEntity>> MALYUTKA = register("malyutka",
            EntityType.Builder.<MalyutkaEntity>of(MalyutkaEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> entityTypeBuilder) {
        return ENTITY_TYPES.register(name, () -> entityTypeBuilder.build(name));

    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
