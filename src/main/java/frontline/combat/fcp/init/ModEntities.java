package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Hellfire.LockOnHellfireEntity;
import frontline.combat.fcp.entity.projectile.Hellfire.WireGuidedHellfireEntity;
import frontline.combat.fcp.entity.projectile.Malyutka.MalyutkaEntity;
import frontline.combat.fcp.entity.projectile.Sidewinder.SidewinderEntity;
import frontline.combat.fcp.entity.vehicle.Aavp.AAVPEntity;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp1.BMP1AMEntity;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp1.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp1.BMP1UEntity;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2Entity;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp1.BMP1PEntity;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2DEntity;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2MEntity;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2NoAtgmEntity;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2MDEntity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR82Entity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR82ATEntity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR3EEntity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR4MV1Entity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR4E1Entity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR4EEntity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR80Entity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR80CopeEntity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR82CopeEntity;
import frontline.combat.fcp.entity.vehicle.Fmtv.FMTVEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrGLEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrDualEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrMGEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrRWSEntity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM60DoorGunsEntity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM60GunshipEntity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM134DoorGunsEntity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM134GunshipEntity;
import frontline.combat.fcp.entity.vehicle.Huey.VenomEntity;
import frontline.combat.fcp.entity.vehicle.Huey.VenomGunshipEntity;
import frontline.combat.fcp.entity.vehicle.Huey.VenomDoorGunsEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CombineEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CultivatorEntity;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeUnarmedEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.JohnDeereEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.SeederEntity;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazEntity;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazKungEntity;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazLongEntity;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdHeavyEntity;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdArmedEntity;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdEntity;
import frontline.combat.fcp.entity.vehicle.M109.M109Entity;
import frontline.combat.fcp.entity.vehicle.Matv.MATV9In1Entity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVCrowsEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVEntity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplTowEntity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplKornetEntity;
import frontline.combat.fcp.entity.vehicle.Stugna.StugnaControllerEntity;
import frontline.combat.fcp.entity.vehicle.Stugna.StugnaLauncherEntity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplDshkEntity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplMk19Entity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplAgs17Entity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplM2Entity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplMg3Entity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplZis3Entity;
import frontline.combat.fcp.entity.vehicle.Msta.MstaEntity;
import frontline.combat.fcp.entity.vehicle.Vena.VenaEntity;
import frontline.combat.fcp.entity.vehicle.Pantsir.PantsirEntity;
import frontline.combat.fcp.entity.vehicle.Brdm.Brdm2Entity;
import frontline.combat.fcp.entity.vehicle.M939.M939Entity;
import frontline.combat.fcp.entity.vehicle.ManHx58.ManHx58Entity;
import frontline.combat.fcp.entity.vehicle.ManHx58.ManHx58Mg3Entity;
import frontline.combat.fcp.entity.vehicle.ManHx60.ManHx60Entity;
import frontline.combat.fcp.entity.vehicle.GenericSuv.GenericSuvEntity;
import frontline.combat.fcp.entity.vehicle.Civ.CivCarEntity;
import frontline.combat.fcp.entity.vehicle.Fmtv4x4.FMTV4x4Entity;
import frontline.combat.fcp.entity.vehicle.Kozak.Kozak5Entity;
import frontline.combat.fcp.entity.vehicle.Kozak.Kozak2m1Entity;
import frontline.combat.fcp.entity.vehicle.Kozak.KozakAmbulanceEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVTOWEntity;
import frontline.combat.fcp.entity.vehicle.Rg33.RG33Entity;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.BigBirdEntity;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.WolfEntity;
import frontline.combat.fcp.entity.vehicle.Mi17.MI17Entity;
import frontline.combat.fcp.entity.vehicle.Mi17.MI17DoorGunsEntity;
import frontline.combat.fcp.entity.vehicle.Mi8.MI8Entity;
import frontline.combat.fcp.entity.vehicle.Mi8.MI8DoorGunsEntity;
import frontline.combat.fcp.entity.vehicle.Mi8.MI8MTVEntity;
import frontline.combat.fcp.entity.vehicle.Mi8.MI8AMTShEntity;
import frontline.combat.fcp.entity.vehicle.Novator.NovatorEntity;
import frontline.combat.fcp.entity.vehicle.NovatorUnarmed.NovatorUnarmedEntity;
import frontline.combat.fcp.entity.vehicle.Dpv.DpvM240Entity;
import frontline.combat.fcp.entity.vehicle.Dpv.DpvMinigunEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerM2Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMGSEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerDragoonEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMk19Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerTowEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMortarEntity;
import frontline.combat.fcp.entity.vehicle.T72.T72Entity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxBMPEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxRocketPodEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxSpg9Entity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxMortarEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxZu23Entity;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import frontline.combat.fcp.entity.vehicle.Type10.Type10Entity;
import frontline.combat.fcp.entity.vehicle.Leopard2.Leopard2Entity;
import frontline.combat.fcp.entity.vehicle.TyphoonVdv.TyphoonVdvEntity;
import frontline.combat.fcp.entity.vehicle.TyphoonVdvUnarmed.TyphoonVdvUnarmedEntity;
import frontline.combat.fcp.entity.vehicle.LandRover110.LandRover110Entity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZDSHKAEntity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZSPG9Entity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZ452Entity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZ3303Entity;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import frontline.combat.fcp.entity.vehicle.Gaz66.Gaz66Entity;
import frontline.combat.fcp.entity.vehicle.Zis5.Zis5Entity;
import frontline.combat.fcp.entity.vehicle.Ural.UralFuelEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralKungEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralGradEntity;
import frontline.combat.fcp.entity.vehicle.Viper.ViperEntity;
import frontline.combat.fcp.entity.vehicle.Oh1.Oh1Entity;
import frontline.combat.fcp.entity.vehicle.Ch53.Ch53aEntity;
import frontline.combat.fcp.entity.vehicle.Ch53.Ch53eEntity;
import frontline.combat.fcp.entity.vehicle.Ch47.Ch47Entity;
import frontline.combat.fcp.entity.vehicle.Puma.PumaEntity;
import frontline.combat.fcp.entity.vehicle.Khrizantema.KhrizantemaEntity;
import frontline.combat.fcp.entity.vehicle.Bmp3.Bmp3Entity;
import frontline.combat.fcp.entity.vehicle.Ch53.Mh53Entity;
import frontline.combat.fcp.entity.vehicle.Uh60.Uh60Entity;
import frontline.combat.fcp.entity.vehicle.Uh60.Uh60MinigunEntity;
import frontline.combat.fcp.entity.vehicle.Uh60.Mh60lEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, FCP.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ToyotaHiluxEntity>> TOYOTA_HILUX = register("toyota_hilux",
            EntityType.Builder.of(ToyotaHiluxEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<DpvM240Entity>> DPV_M240 = register("dpv_m240",
            EntityType.Builder.of(DpvM240Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<DpvMinigunEntity>> DPV_MINIGUN = register("dpv_minigun",
            EntityType.Builder.of(DpvMinigunEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<ToyotaHiluxRocketPodEntity>> TOYOTA_HILUX_ROCKET_POD = register("toyota_hilux_rocket_pod",
            EntityType.Builder.of(ToyotaHiluxRocketPodEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<ToyotaHiluxBMPEntity>> TOYOTA_HILUX_BMP = register("toyota_hilux_bmp",
            EntityType.Builder.of(ToyotaHiluxBMPEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<ToyotaHiluxSpg9Entity>> TOYOTA_HILUX_SPG9 = register("toyota_hilux_spg9",
            EntityType.Builder.of(ToyotaHiluxSpg9Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<ToyotaHiluxSpg9Entity>> TOYOTA_HILUX_KORNET = register("toyota_hilux_kornet",
            EntityType.Builder.of(ToyotaHiluxSpg9Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<ToyotaHiluxMortarEntity>> TOYOTA_HILUX_MORTAR = register("toyota_hilux_mortar",
            EntityType.Builder.of(ToyotaHiluxMortarEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<ToyotaHiluxZu23Entity>> TOYOTA_HILUX_ZU23 = register("toyota_hilux_zu23",
            EntityType.Builder.of(ToyotaHiluxZu23Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<UAZEntity>> UAZ = register("uaz",
            EntityType.Builder.of(UAZEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<UAZDSHKAEntity>> UAZ_DSHKA = register("uaz_dshka",
            EntityType.Builder.of(UAZDSHKAEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<UAZSPG9Entity>> UAZ_SPG9 = register("uaz_spg9",
            EntityType.Builder.of(UAZSPG9Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<UAZ452Entity>> UAZ_452 = register("uaz_452",
            EntityType.Builder.of(UAZ452Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<UAZ3303Entity>> UAZ_3303 = register("uaz_3303",
            EntityType.Builder.of(UAZ3303Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<StrykerMGSEntity>> STRYKER_MGS = register("stryker_mgs",
            EntityType.Builder.of(StrykerMGSEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<StrykerM2Entity>> STRYKER_M2 = register("stryker_m2",
            EntityType.Builder.of(StrykerM2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<StrykerDragoonEntity>> STRYKER_DRAGOON = register("stryker_dragoon",
            EntityType.Builder.of(StrykerDragoonEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<StrykerMk19Entity>> STRYKER_MK19 = register("stryker_mk19",
            EntityType.Builder.of(StrykerMk19Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<StrykerTowEntity>> STRYKER_TOW = register("stryker_tow",
            EntityType.Builder.of(StrykerTowEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<StrykerMortarEntity>> STRYKER_MORTAR = register("stryker_mortar",
            EntityType.Builder.of(StrykerMortarEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<LittlebirdEntity>> LITTLEBIRD = register("littlebird",
            EntityType.Builder.of(LittlebirdEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<LittlebirdArmedEntity>> LITTLEBIRD_ARMED = register("littlebird_armed",
            EntityType.Builder.of(LittlebirdArmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<LittlebirdHeavyEntity>> LITTLEBIRD_HEAVY = register("littlebird_heavy",
            EntityType.Builder.of(LittlebirdHeavyEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BMP1Entity>> BMP1 = register("bmp1",
            EntityType.Builder.of(BMP1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BMP1UEntity>> BMP1U = register("bmp1u",
            EntityType.Builder.of(BMP1UEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BMP1AMEntity>> BMP1AM = register("bmp1am",
            EntityType.Builder.of(BMP1AMEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BMP2Entity>> BMP2 = register("bmp2",
            EntityType.Builder.of(BMP2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    // Puma IFV: one entity class for both variants (the registry name picks geo and skins).
    public static final DeferredHolder<EntityType<?>, EntityType<PumaEntity>> PUMA = register("puma",
            EntityType.Builder.of(PumaEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<PumaEntity>> PUMA_ATGM = register("puma_atgm",
            EntityType.Builder.of(PumaEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Marder.MarderEntity>> MARDER1A3 = register("marder1a3",
            EntityType.Builder.of(frontline.combat.fcp.entity.vehicle.Marder.MarderEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Bradley.BradleyEntity>> M2A2 = bradley("m2a2");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Bradley.BradleyEntity>> M6A2 = bradley("m6a2");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Bradley.BradleyEntity>> M2A2_BUSK = bradley("m2a2_busk");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Bradley.BradleyEntity>> M2A3 = bradley("m2a3");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Bradley.BradleyEntity>> M2A3_BUSK = bradley("m2a3_busk");
    // 9P157-2 Khrizantema-S ATGM carrier (BMP-3 hull).
    public static final DeferredHolder<EntityType<?>, EntityType<KhrizantemaEntity>> KHRIZANTEMA = register("khrizantema",
            EntityType.Builder.of(KhrizantemaEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3.4f, 2.3f));
    // BMP-3 family: one entity class for all four (the registry name picks geo and skins).
    public static final DeferredHolder<EntityType<?>, EntityType<Bmp3Entity>> BMP3 = register("bmp3",
            EntityType.Builder.of(Bmp3Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3.4f, 2.3f));
    public static final DeferredHolder<EntityType<?>, EntityType<Bmp3Entity>> BMP3_CAGE = register("bmp3_cage",
            EntityType.Builder.of(Bmp3Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3.4f, 2.3f));
    public static final DeferredHolder<EntityType<?>, EntityType<Bmp3Entity>> BMP3M = register("bmp3m",
            EntityType.Builder.of(Bmp3Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3.4f, 2.3f));
    public static final DeferredHolder<EntityType<?>, EntityType<Bmp3Entity>> BRM3K = register("brm3k",
            EntityType.Builder.of(Bmp3Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3.4f, 2.3f));
    public static final DeferredHolder<EntityType<?>, EntityType<BMP1PEntity>> BMP1P = register("bmp1p",
            EntityType.Builder.of(BMP1PEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BMP2DEntity>> BMP2D = register("bmp2d",
            EntityType.Builder.of(BMP2DEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BMP2MEntity>> BMP2M = register("bmp2m",
            EntityType.Builder.of(BMP2MEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BMP2NoAtgmEntity>> BMP2_NOATGM = register("bmp2_noatgm",
            EntityType.Builder.of(BMP2NoAtgmEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BMP2MDEntity>> BMP2MD = register("bmp2md",
            EntityType.Builder.of(BMP2MDEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<AAVPEntity>> AAVP = register("aavp",
            EntityType.Builder.of(AAVPEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Lav25Entity>> LAV25 = register("lav25",
            EntityType.Builder.of(Lav25Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Lav25Entity>> LAV25_MG = register("lav25_mg",
            EntityType.Builder.of(Lav25Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72A = register("t72a",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72AV = register("t72av",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72B = register("t72b",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72B_1985 = register("t72b_1985",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72B_1989 = register("t72b_1989",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72B2 = register("t72b2",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72B3 = register("t72b3",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72B3_2016 = register("t72b3_2016",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72B3M = register("t72b3m",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T72M2_MODERNA = register("t72m2_moderna",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<T72Entity>> T90A = register("t90a",
            EntityType.Builder.of(T72Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.T90M.T90MEntity>> T90M = t90m("t90m");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.T90M.T90MEntity>> T90M_2022 = t90m("t90m_2022");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.T90M.T90MEntity>> T90M_2024 = t90m("t90m_2024");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.T90M.T90MEntity>> T90MS = t90m("t90ms");
    public static final DeferredHolder<EntityType<?>, EntityType<UralEntity>> URAL = register("ural",
            EntityType.Builder.of(UralEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Gaz66Entity>> GAZ66 = register("gaz66",
            EntityType.Builder.of(Gaz66Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<Zis5Entity>> ZIS5 = register("zis5",
            EntityType.Builder.of(Zis5Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<UralFuelEntity>> URAL_FUEL = register("ural_fuel",
            EntityType.Builder.of(UralFuelEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<UralKungEntity>> URAL_KUNG = register("ural_kung",
            EntityType.Builder.of(UralKungEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<UralGradEntity>> URAL_GRAD = register("ural_grad",
            EntityType.Builder.of(UralGradEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity>> HIMARS = register("himars",
            EntityType.Builder.of(frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<KamazEntity>> KAMAZ = register("kamaz",
            EntityType.Builder.of(KamazEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<KamazKungEntity>> KAMAZ_KUNG = register("kamaz_kung",
            EntityType.Builder.of(KamazKungEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<KamazLongEntity>> KAMAZ_LONG = register("kamaz_long",
            EntityType.Builder.of(KamazLongEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<ViperEntity>> VIPER = register("viper",
            EntityType.Builder.of(ViperEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Ka52.Ka52Entity>> KA52 = register("ka52",
            EntityType.Builder.of(frontline.combat.fcp.entity.vehicle.Ka52.Ka52Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f, 2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<Oh1Entity>> OH1 = register("oh1",
            EntityType.Builder.of(Oh1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Ch53aEntity>> CH53A = register("ch53a",
            EntityType.Builder.of(Ch53aEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Ch53eEntity>> CH53E = register("ch53e",
            EntityType.Builder.of(Ch53eEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Ch47Entity>> CH47 = register("ch47",
            EntityType.Builder.of(Ch47Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Mh53Entity>> MH53 = register("mh53",
            EntityType.Builder.of(Mh53Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Uh60Entity>> UH60 = register("uh60",
            EntityType.Builder.of(Uh60Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Uh60MinigunEntity>> UH60_MINIGUN = register("uh60_minigun",
            EntityType.Builder.of(Uh60MinigunEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Mh60lEntity>> MH60L = register("mh60l",
            EntityType.Builder.of(Mh60lEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<GazTigrEntity>> GAZ_TIGR = register("gaz_tigr",
            EntityType.Builder.of(GazTigrEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<GazTigrRWSEntity>> GAZ_TIGR_RWS = register("gaz_tigr_rws",
            EntityType.Builder.of(GazTigrRWSEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<GazTigrMGEntity>> GAZ_TIGR_MG = register("gaz_tigr_mg",
            EntityType.Builder.of(GazTigrMGEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<GazTigrGLEntity>> GAZ_TIGR_GL = register("gaz_tigr_gl",
            EntityType.Builder.of(GazTigrGLEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<GazTigrDualEntity>> GAZ_TIGR_DUAL = register("gaz_tigr_dual",
            EntityType.Builder.of(GazTigrDualEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HueyEntity>> HUEY = register("huey",
            EntityType.Builder.of(HueyEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HueyM60DoorGunsEntity>> HUEY_M60_DOOR_GUNS = register("huey_m60_door_guns",
            EntityType.Builder.of(HueyM60DoorGunsEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HueyM60GunshipEntity>> HUEY_M60_GUNSHIP = register("huey_m60_gunship",
            EntityType.Builder.of(HueyM60GunshipEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HueyM134DoorGunsEntity>> HUEY_M134_DOOR_GUNS = register("huey_m134_door_guns",
            EntityType.Builder.of(HueyM134DoorGunsEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HueyM134GunshipEntity>> HUEY_M134_GUNSHIP = register("huey_m134_gunship",
            EntityType.Builder.of(HueyM134GunshipEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<VenomEntity>> VENOM = register("venom",
            EntityType.Builder.of(VenomEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<VenomGunshipEntity>> VENOM_GUNSHIP = register("venom_gunship",
            EntityType.Builder.of(VenomGunshipEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<VenomDoorGunsEntity>> VENOM_DOOR_GUNS = register("venom_door_guns",
            EntityType.Builder.of(VenomDoorGunsEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<NovatorEntity>> NOVATOR = register("novator",
            EntityType.Builder.of(NovatorEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<NovatorUnarmedEntity>> NOVATOR_UNARMED = register("novator_unarmed",
            EntityType.Builder.of(NovatorUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<NovatorEntity>> NOVATOR_STUGNA = register("novator_stugna",
            EntityType.Builder.of(NovatorEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MATVEntity>> MATV = register("matv",
            EntityType.Builder.of(MATVEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<RG33Entity>> RG_33 = register("rg_33",
            EntityType.Builder.of(RG33Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<EmplTowEntity>> EMPL_TOW = register("empl_tow",
            EntityType.Builder.of(EmplTowEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,1.4f));
    public static final DeferredHolder<EntityType<?>, EntityType<EmplKornetEntity>> EMPL_KORNET = register("empl_kornet",
            EntityType.Builder.of(EmplKornetEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,1.4f));
    public static final DeferredHolder<EntityType<?>, EntityType<StugnaLauncherEntity>> STUGNA_LAUNCHER = register("stugna_launcher",
            EntityType.Builder.of(StugnaLauncherEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,1.4f));
    public static final DeferredHolder<EntityType<?>, EntityType<StugnaControllerEntity>> STUGNA_CONTROLLER = register("stugna_controller",
            EntityType.Builder.of(StugnaControllerEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.6f));
    public static final DeferredHolder<EntityType<?>, EntityType<EmplDshkEntity>> EMPL_DSHK = register("empl_dshk",
            EntityType.Builder.of(EmplDshkEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,1.4f));
    public static final DeferredHolder<EntityType<?>, EntityType<EmplMk19Entity>> EMPL_MK19 = register("empl_mk19",
            EntityType.Builder.of(EmplMk19Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.84f));
    public static final DeferredHolder<EntityType<?>, EntityType<EmplAgs17Entity>> EMPL_AGS17 = register("empl_ags17",
            EntityType.Builder.of(EmplAgs17Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.84f));
    public static final DeferredHolder<EntityType<?>, EntityType<EmplM2Entity>> EMPL_M2 = register("empl_m2",
            EntityType.Builder.of(EmplM2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.84f));
    public static final DeferredHolder<EntityType<?>, EntityType<EmplMg3Entity>> EMPL_MG3 = register("empl_mg3",
            EntityType.Builder.of(EmplMg3Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.84f));
    public static final DeferredHolder<EntityType<?>, EntityType<EmplZis3Entity>> EMPL_ZIS3 = register("empl_zis3",
            EntityType.Builder.of(EmplZis3Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.84f));
    public static final DeferredHolder<EntityType<?>, EntityType<PantsirEntity>> PANTSIR = register("pantsir",
            EntityType.Builder.of(PantsirEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(4f,4f));
    public static final DeferredHolder<EntityType<?>, EntityType<Brdm2Entity>> BRDM2 = register("brdm2",
            EntityType.Builder.of(Brdm2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<Brdm2Entity>> BRDM2_KORNET = register("brdm2_kornet",
            EntityType.Builder.of(Brdm2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.M113.M113Entity>> M113 = m113("m113");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.M113.M113Entity>> M113A2 = m113("m113a2");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.M113.M113Entity>> SKIF = m113("skif");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Komatsu.KomatsuLavEntity>> KOMATSU_LAV = komatsu("komatsu_lav");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Komatsu.KomatsuLavEntity>> KOMATSU_LAV_SHIELD = komatsu("komatsu_lav_shield");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Komatsu.KomatsuLavEntity>> KOMATSU_LAV_UNARMED = komatsu("komatsu_lav_unarmed");
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.projectile.FcpMineEntity>> MINE = register("mine",
            EntityType.Builder.<frontline.combat.fcp.entity.projectile.FcpMineEntity>of(frontline.combat.fcp.entity.projectile.FcpMineEntity::new, MobCategory.MISC)
                    .setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.15f));
    public static final DeferredHolder<EntityType<?>, EntityType<M939Entity>> M939 = register("m939",
            EntityType.Builder.of(M939Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<ManHx58Entity>> MAN_HX58 = register("man_hx58",
            EntityType.Builder.of(ManHx58Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<ManHx58Mg3Entity>> MAN_HX58_MG3 = register("man_hx58_mg3",
            EntityType.Builder.of(ManHx58Mg3Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<ManHx60Entity>> MAN_HX60 = register("man_hx60",
            EntityType.Builder.of(ManHx60Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<GenericSuvEntity>> GENERIC_SUV = register("generic_suv",
            EntityType.Builder.of(GenericSuvEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<CivCarEntity>> BMW_E34 = register("bmw_e34",
            EntityType.Builder.of(CivCarEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<CivCarEntity>> LAND_CRUISER_80 = register("land_cruiser_80",
            EntityType.Builder.of(CivCarEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<CivCarEntity>> SUBURBAN = register("suburban",
            EntityType.Builder.of(CivCarEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<CivCarEntity>> VW_T4 = register("vw_t4",
            EntityType.Builder.of(CivCarEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<FMTV4x4Entity>> FMTV_4X4 = register("fmtv_4x4",
            EntityType.Builder.of(FMTV4x4Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Kozak5Entity>> KOZAK5 = register("kozak5",
            EntityType.Builder.of(Kozak5Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Kozak2m1Entity>> KOZAK2M1 = register("kozak2m1",
            EntityType.Builder.of(Kozak2m1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<KozakAmbulanceEntity>> KOZAK_AMBULANCE = register("kozak_ambulance",
            EntityType.Builder.of(KozakAmbulanceEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MATVTOWEntity>> MATV_TOW = register("matv_tow",
            EntityType.Builder.of(MATVTOWEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MATVCrowsEntity>> MATV_CROW = register("matv_crow",
            EntityType.Builder.of(MATVCrowsEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MATV9In1Entity>> MATV_9IN1 = register("matv_9in1",
            EntityType.Builder.of(MATV9In1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MATV9In1Entity>> MATV_MK19 = register("matv_mk19",
            EntityType.Builder.of(MATV9In1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    // ATF Dingo: one model, three gun variants, on the shared attachment entity (like the Caiman).
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> DINGO = register("dingo",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> DINGO_MG3 = register("dingo_mg3",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> DINGO_M2 = register("dingo_m2",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> DINGO_GMG = register("dingo_gmg",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    // Type 73 light truck: unarmed (rear hard top toggle) and M2, on the shared attachment entity (like the Dingo).
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> TYPE73 = register("type73",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> TYPE73_M2 = register("type73_m2",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_AMBULANCE = register("hmmwv_ambulance",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_ARMORED_M2 = register("hmmwv_armored_m2",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_ARMORED_MK19 = register("hmmwv_armored_mk19",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_ARMORED_UNARMED = register("hmmwv_armored_unarmed",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_ASRAD = register("hmmwv_asrad",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_AVENGER = register("hmmwv_avenger",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_CARGO = register("hmmwv_cargo",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_SHELTER = register("hmmwv_shelter",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_SOFT_TOP = register("hmmwv_soft_top",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_SOFT_TOP_NO_DOORS = register("hmmwv_soft_top_no_doors",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    // Toyota HMV (JGSDF Kokido): shares the Humvee entity (steering, wheels, skins); no attachments.
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> TOYOTA_HMV = register("toyota_hmv",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_M2 = register("hmmwv_unarmored_m2",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_M2_SHIELD = register("hmmwv_unarmored_m2_shield",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_M2_TURRET = register("hmmwv_unarmored_m2_turret",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_TOW = register("hmmwv_unarmored_tow",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_TOW_TURRET = register("hmmwv_unarmored_tow_turret",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_UNARMED = register("hmmwv_unarmored_unarmed",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    // Caiman MRAP: shares the Humvee attachment entity (roof rack and gun shield toggles).
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> CAIMAN = register("caiman",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> CAIMAN_UNARMED = register("caiman_unarmed",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> CAIMAN_POLICE = register("caiman_police",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> CAIMAN_UPARMORED = register("caiman_uparmored",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<HumveeUnarmedEntity>> CAIMAN_UPARMORED_UNARMED = register("caiman_uparmored_unarmed",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<BTR3EEntity>> BTR3E = register("btr3e",
            EntityType.Builder.of(BTR3EEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BTR4MV1Entity>> BTR4MV1 = register("btr4mv1",
            EntityType.Builder.of(BTR4MV1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BTR4E1Entity>> BTR4E1 = register("btr4e1",
            EntityType.Builder.of(BTR4E1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BTR4EEntity>> BTR4E = register("btr4e",
            EntityType.Builder.of(BTR4EEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BTR82Entity>> BTR82 = register("btr82",
            EntityType.Builder.of(BTR82Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BTR82ATEntity>> BTR82AT = register("btr82at",
            EntityType.Builder.of(BTR82ATEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BTR80Entity>> BTR80 = register("btr80",
            EntityType.Builder.of(BTR80Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BTR80CopeEntity>> BTR80_COPE = register("btr80_cope",
            EntityType.Builder.of(BTR80CopeEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<BTR82CopeEntity>> BTR82_COPE = register("btr82_cope",
            EntityType.Builder.of(BTR82CopeEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<FMTVEntity>> FMTV = register("fmtv",
            EntityType.Builder.of(FMTVEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<M1a1Entity>> M1A1 = register("m1a1",
            EntityType.Builder.of(M1a1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    // Abrams family: one entity class; the registry name picks geo, skins and attachments.
    public static final DeferredHolder<EntityType<?>, EntityType<M1a1Entity>> M1A1_AIM = register("m1a1_aim",
            EntityType.Builder.of(M1a1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<M1a1Entity>> M1A1_UKR = register("m1a1_ukr",
            EntityType.Builder.of(M1a1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<M1a1Entity>> M1A2 = register("m1a2",
            EntityType.Builder.of(M1a1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<M1a1Entity>> M1A2_TUSK1 = register("m1a2_tusk1",
            EntityType.Builder.of(M1a1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<M1a1Entity>> M1A2_TUSK2 = register("m1a2_tusk2",
            EntityType.Builder.of(M1a1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final DeferredHolder<EntityType<?>, EntityType<Type10Entity>> TYPE10 = register("type10",
            EntityType.Builder.of(Type10Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));

    public static final DeferredHolder<EntityType<?>, EntityType<Leopard2Entity>> LEOPARD2A4 = register("leopard2a4",
            EntityType.Builder.of(Leopard2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Leopard2Entity>> LEOPARD2A4_UKR = register("leopard2a4_ukr",
            EntityType.Builder.of(Leopard2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Leopard2Entity>> LEOPARD2A6 = register("leopard2a6",
            EntityType.Builder.of(Leopard2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<Leopard2Entity>> LEOPARD2A7V = register("leopard2a7v",
            EntityType.Builder.of(Leopard2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<TyphoonVdvEntity>> TYPHOON_VDV = register("typhoon_vdv",
            EntityType.Builder.of(TyphoonVdvEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<TyphoonVdvUnarmedEntity>> TYPHOON_VDV_UNARMED = register("typhoon_vdv_unarmed",
            EntityType.Builder.of(TyphoonVdvUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final DeferredHolder<EntityType<?>, EntityType<LandRover110Entity>> LANDROVER_110 = register("landrover_110",
            EntityType.Builder.of(LandRover110Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    // Projectiles
    public static final DeferredHolder<EntityType<?>, EntityType<LockOnHellfireEntity>> LOCK_ON_HELLFIRE = register("lock_on_hellfire",
            EntityType.Builder.<LockOnHellfireEntity>of(LockOnHellfireEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    // HIMARS GMLRS: TV- or coordinate-guided, steers like the Hellfires.
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity>> HIMARS_MISSILE = register("himars_missile",
            EntityType.Builder.<frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity>of(frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    // Radar-guided 9M123 (Khrizantema): lock-on homing, flies and looks like SBW's wire-guided missile.
    public static final DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.projectile.Khrizantema.LockOn9M123Entity>> LOCK_ON_9M123 = register("lock_on_9m123",
            EntityType.Builder.<frontline.combat.fcp.entity.projectile.Khrizantema.LockOn9M123Entity>of(frontline.combat.fcp.entity.projectile.Khrizantema.LockOn9M123Entity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<WireGuidedHellfireEntity>> WIRE_GUIDED_HELLFIRE = register("wire_guided_hellfire",
            EntityType.Builder.<WireGuidedHellfireEntity>of(WireGuidedHellfireEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<SidewinderEntity>> SIDEWINDER = register("sidewinder",
            EntityType.Builder.<SidewinderEntity>of(SidewinderEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<MalyutkaEntity>> MALYUTKA = register("malyutka",
            EntityType.Builder.<MalyutkaEntity>of(MalyutkaEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    // Meme Vehicles
    public static final DeferredHolder<EntityType<?>, EntityType<BigBirdEntity>> BIGBIRD = register("bigbird",
            EntityType.Builder.of(BigBirdEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<WolfEntity>> T14_ARMATA = register("t14_armata",
            EntityType.Builder.of(WolfEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.5f,0.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<M109Entity>> M109 = register("m109",
            EntityType.Builder.of(M109Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MstaEntity>> MSTA = register("msta",
            EntityType.Builder.of(MstaEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(4f, 3f));
    // 2S31 Vena: BMP-3 hull, 120 mm gun-mortar artillery.
    public static final DeferredHolder<EntityType<?>, EntityType<VenaEntity>> VENA = register("2s31",
            EntityType.Builder.of(VenaEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3.4f, 2.3f));
    public static final DeferredHolder<EntityType<?>, EntityType<MI17Entity>> MI17 = register("mi17",
            EntityType.Builder.of(MI17Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MI8Entity>> MI8 = register("mi8",
            EntityType.Builder.of(MI8Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MI17DoorGunsEntity>> MI17_DOOR_GUNS = register("mi17_door_guns",
            EntityType.Builder.of(MI17DoorGunsEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MI8DoorGunsEntity>> MI8_DOOR_GUNS = register("mi8_door_guns",
            EntityType.Builder.of(MI8DoorGunsEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MI8MTVEntity>> MI8_MTV = register("mi8_mtv",
            EntityType.Builder.of(MI8MTVEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<MI8AMTShEntity>> MI8_AMTSH = register("mi8_amtsh",
            EntityType.Builder.of(MI8AMTShEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final DeferredHolder<EntityType<?>, EntityType<JohnDeereEntity>> JOHN_DEERE = register("john_deere",
            EntityType.Builder.of(JohnDeereEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f, 3f));
    public static final DeferredHolder<EntityType<?>, EntityType<CombineEntity>> COMBINE = register("combine",
            EntityType.Builder.of(CombineEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f, 3f));
    public static final DeferredHolder<EntityType<?>, EntityType<SeederEntity>> SEEDER = register("seeder",
            EntityType.Builder.of(SeederEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(4f, 3f));
    public static final DeferredHolder<EntityType<?>, EntityType<CultivatorEntity>> CULTIVATOR = register("cultivator",
            EntityType.Builder.of(CultivatorEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(4f, 3f));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.Builder<T> entityTypeBuilder) {
        return ENTITY_TYPES.register(name, () -> entityTypeBuilder.build(FCP.MODID + ":" + name));
    }

    /** One of the T-90M variants (all on T90MEntity). */
    private static DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.T90M.T90MEntity>> t90m(String name) {
        return register(name, EntityType.Builder.of(frontline.combat.fcp.entity.vehicle.T90M.T90MEntity::new, MobCategory.MISC)
                .setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f, 2f));
    }

    /** One of the M2 Bradley variants (all on BradleyEntity). */
    private static DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Bradley.BradleyEntity>> bradley(String name) {
        return register(name, EntityType.Builder.of(frontline.combat.fcp.entity.vehicle.Bradley.BradleyEntity::new, MobCategory.MISC)
                .setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f, 2f));
    }

    private static DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.M113.M113Entity>> m113(String name) {
        return register(name, EntityType.Builder.of(frontline.combat.fcp.entity.vehicle.M113.M113Entity::new, MobCategory.MISC)
                .setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f, 2f));
    }

    private static DeferredHolder<EntityType<?>, EntityType<frontline.combat.fcp.entity.vehicle.Komatsu.KomatsuLavEntity>> komatsu(String name) {
        return register(name, EntityType.Builder.of(frontline.combat.fcp.entity.vehicle.Komatsu.KomatsuLavEntity::new, MobCategory.MISC)
                .setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2.5f, 2f));
    }

    // Trailers
    public static final DeferredHolder<EntityType<?>, EntityType<ExampleTrailerEntity>> EXAMPLE_TRAILER = register("example_trailer",
            EntityType.Builder.of(ExampleTrailerEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(5f, 3f));
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
