package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Hellfire.LockOnHellfireEntity;
import frontline.combat.fcp.entity.projectile.Hellfire.WireGuidedHellfireEntity;
import frontline.combat.fcp.entity.projectile.Malyutka.MalyutkaEntity;
import frontline.combat.fcp.entity.projectile.Sidewinder.SidewinderEntity;
import frontline.combat.fcp.entity.vehicle.Aavp.AAVPEntity;
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
import frontline.combat.fcp.entity.vehicle.Btr.BTR80Entity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR80CopeEntity;
import frontline.combat.fcp.entity.vehicle.Btr.BTR82CopeEntity;
import frontline.combat.fcp.entity.vehicle.Fmtv.FMTVEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrGLEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrMGEntity;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrRWSEntity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyDoorGunnerM134Entity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyDoorGunnerM60Entity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyRocketsEntity;
import frontline.combat.fcp.entity.vehicle.Huey.VenomEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CombineEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CultivatorEntity;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeUnarmedEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.JohnDeereEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.SeederEntity;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazEntity;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazKungEntity;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazLongEntity;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdArmedEntity;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdEntity;
import frontline.combat.fcp.entity.vehicle.M109.M109Entity;
import frontline.combat.fcp.entity.vehicle.Matv.MATV9In1Entity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVCrowsEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVEntity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplTowEntity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplKornetEntity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplDshkEntity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplMk19Entity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplAgs17Entity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplM2Entity;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplZis3Entity;
import frontline.combat.fcp.entity.vehicle.Pantsir.PantsirEntity;
import frontline.combat.fcp.entity.vehicle.Brdm.Brdm2Entity;
import frontline.combat.fcp.entity.vehicle.M939.M939Entity;
import frontline.combat.fcp.entity.vehicle.Kozak.Kozak5Entity;
import frontline.combat.fcp.entity.vehicle.Kozak.Kozak2m1Entity;
import frontline.combat.fcp.entity.vehicle.Kozak.KozakAmbulanceEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVTOWEntity;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.BigBirdEntity;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.WolfEntity;
import frontline.combat.fcp.entity.vehicle.Mi17.MI17Entity;
import frontline.combat.fcp.entity.vehicle.Novator.NovatorEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerM2Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMGSEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerDragoonEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMk19Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerTowEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMortarEntity;
import frontline.combat.fcp.entity.vehicle.T72av.T72AVEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxBMPEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxRocketPodEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxSpg9Entity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxMortarEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxZu23Entity;
import frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer.ExampleTrailerEntity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZDSHKAEntity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZSPG9Entity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralFuelEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralKungEntity;
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
    public static final RegistryObject<EntityType<ToyotaHiluxMortarEntity>> TOYOTA_HILUX_MORTAR = register("toyota_hilux_mortar",
            EntityType.Builder.of(ToyotaHiluxMortarEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<ToyotaHiluxZu23Entity>> TOYOTA_HILUX_ZU23 = register("toyota_hilux_zu23",
            EntityType.Builder.of(ToyotaHiluxZu23Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<UAZEntity>> UAZ = register("uaz",
            EntityType.Builder.of(UAZEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final RegistryObject<EntityType<UAZDSHKAEntity>> UAZ_DSHKA = register("uaz_dshka",
            EntityType.Builder.of(UAZDSHKAEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final RegistryObject<EntityType<UAZSPG9Entity>> UAZ_SPG9 = register("uaz_spg9",
            EntityType.Builder.of(UAZSPG9Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final RegistryObject<EntityType<StrykerMGSEntity>> STRYKER_MGS = register("stryker_mgs",
            EntityType.Builder.of(StrykerMGSEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<StrykerM2Entity>> STRYKER_M2 = register("stryker_m2",
            EntityType.Builder.of(StrykerM2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<StrykerDragoonEntity>> STRYKER_DRAGOON = register("stryker_dragoon",
            EntityType.Builder.of(StrykerDragoonEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<StrykerMk19Entity>> STRYKER_MK19 = register("stryker_mk19",
            EntityType.Builder.of(StrykerMk19Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<StrykerTowEntity>> STRYKER_TOW = register("stryker_tow",
            EntityType.Builder.of(StrykerTowEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<StrykerMortarEntity>> STRYKER_MORTAR = register("stryker_mortar",
            EntityType.Builder.of(StrykerMortarEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<LittlebirdEntity>> LITTLEBIRD = register("littlebird",
            EntityType.Builder.of(LittlebirdEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final RegistryObject<EntityType<LittlebirdArmedEntity>> LITTLEBIRD_ARMED = register("littlebird_armed",
            EntityType.Builder.of(LittlebirdArmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final RegistryObject<EntityType<BMP1Entity>> BMP1 = register("bmp1",
            EntityType.Builder.of(BMP1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BMP1UEntity>> BMP1U = register("bmp1u",
            EntityType.Builder.of(BMP1UEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BMP1AMEntity>> BMP1AM = register("bmp1am",
            EntityType.Builder.of(BMP1AMEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BMP2Entity>> BMP2 = register("bmp2",
            EntityType.Builder.of(BMP2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BMP1PEntity>> BMP1P = register("bmp1p",
            EntityType.Builder.of(BMP1PEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BMP2DEntity>> BMP2D = register("bmp2d",
            EntityType.Builder.of(BMP2DEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BMP2MEntity>> BMP2M = register("bmp2m",
            EntityType.Builder.of(BMP2MEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BMP2NoAtgmEntity>> BMP2_NOATGM = register("bmp2_noatgm",
            EntityType.Builder.of(BMP2NoAtgmEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BMP2MDEntity>> BMP2MD = register("bmp2md",
            EntityType.Builder.of(BMP2MDEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<AAVPEntity>> AAVP = register("aavp",
            EntityType.Builder.of(AAVPEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<Lav25Entity>> LAV25 = register("lav25",
            EntityType.Builder.of(Lav25Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<T72AVEntity>> T72AV = register("t72av",
            EntityType.Builder.of(T72AVEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<UralEntity>> URAL = register("ural",
            EntityType.Builder.of(UralEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<UralFuelEntity>> URAL_FUEL = register("ural_fuel",
            EntityType.Builder.of(UralFuelEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final RegistryObject<EntityType<UralKungEntity>> URAL_KUNG = register("ural_kung",
            EntityType.Builder.of(UralKungEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final RegistryObject<EntityType<UralGradEntity>> URAL_GRAD = register("ural_grad",
            EntityType.Builder.of(UralGradEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<KamazEntity>> KAMAZ = register("kamaz",
            EntityType.Builder.of(KamazEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<KamazKungEntity>> KAMAZ_KUNG = register("kamaz_kung",
            EntityType.Builder.of(KamazKungEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final RegistryObject<EntityType<KamazLongEntity>> KAMAZ_LONG = register("kamaz_long",
            EntityType.Builder.of(KamazLongEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
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
    public static final RegistryObject<EntityType<HueyEntity>> HUEY = register("huey",
            EntityType.Builder.of(HueyEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HueyRocketsEntity>> HUEY_ROCKETS = register("huey_rockets",
            EntityType.Builder.of(HueyRocketsEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HueyDoorGunnerM60Entity>> HUEY_DOOR_GUNNER_M60 = register("huey_door_gunner_m60",
            EntityType.Builder.of(HueyDoorGunnerM60Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HueyDoorGunnerM134Entity>> HUEY_DOOR_GUNNER_M134 = register("huey_door_gunner_m134",
            EntityType.Builder.of(HueyDoorGunnerM134Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<VenomEntity>> VENOM = register("venom",
            EntityType.Builder.of(VenomEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<NovatorEntity>> NOVATOR = register("novator",
            EntityType.Builder.of(NovatorEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<MATVEntity>> MATV = register("matv",
            EntityType.Builder.of(MATVEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<EmplTowEntity>> EMPL_TOW = register("empl_tow",
            EntityType.Builder.of(EmplTowEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,1.4f));
    public static final RegistryObject<EntityType<EmplKornetEntity>> EMPL_KORNET = register("empl_kornet",
            EntityType.Builder.of(EmplKornetEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,1.4f));
    public static final RegistryObject<EntityType<EmplDshkEntity>> EMPL_DSHK = register("empl_dshk",
            EntityType.Builder.of(EmplDshkEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,1.4f));
    public static final RegistryObject<EntityType<EmplMk19Entity>> EMPL_MK19 = register("empl_mk19",
            EntityType.Builder.of(EmplMk19Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.84f));
    public static final RegistryObject<EntityType<EmplAgs17Entity>> EMPL_AGS17 = register("empl_ags17",
            EntityType.Builder.of(EmplAgs17Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.84f));
    public static final RegistryObject<EntityType<EmplM2Entity>> EMPL_M2 = register("empl_m2",
            EntityType.Builder.of(EmplM2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.84f));
    public static final RegistryObject<EntityType<EmplZis3Entity>> EMPL_ZIS3 = register("empl_zis3",
            EntityType.Builder.of(EmplZis3Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.8f,0.84f));
    public static final RegistryObject<EntityType<PantsirEntity>> PANTSIR = register("pantsir",
            EntityType.Builder.of(PantsirEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(4f,4f));
    public static final RegistryObject<EntityType<Brdm2Entity>> BRDM2 = register("brdm2",
            EntityType.Builder.of(Brdm2Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2.5f));
    public static final RegistryObject<EntityType<M939Entity>> M939 = register("m939",
            EntityType.Builder.of(M939Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,3f));
    public static final RegistryObject<EntityType<Kozak5Entity>> KOZAK5 = register("kozak5",
            EntityType.Builder.of(Kozak5Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<Kozak2m1Entity>> KOZAK2M1 = register("kozak2m1",
            EntityType.Builder.of(Kozak2m1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<KozakAmbulanceEntity>> KOZAK_AMBULANCE = register("kozak_ambulance",
            EntityType.Builder.of(KozakAmbulanceEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<MATVTOWEntity>> MATV_TOW = register("matv_tow",
            EntityType.Builder.of(MATVTOWEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<MATVCrowsEntity>> MATV_CROW = register("matv_crow",
            EntityType.Builder.of(MATVCrowsEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<MATV9In1Entity>> MATV_9IN1 = register("matv_9in1",
            EntityType.Builder.of(MATV9In1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_AMBULANCE = register("hmmwv_ambulance",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_ARMORED_M2 = register("hmmwv_armored_m2",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_ARMORED_MK19 = register("hmmwv_armored_mk19",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_ARMORED_UNARMED = register("hmmwv_armored_unarmed",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_ASRAD = register("hmmwv_asrad",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_AVENGER = register("hmmwv_avenger",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_CARGO = register("hmmwv_cargo",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_SHELTER = register("hmmwv_shelter",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_SOFT_TOP = register("hmmwv_soft_top",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_SOFT_TOP_NO_DOORS = register("hmmwv_soft_top_no_doors",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_M2 = register("hmmwv_unarmored_m2",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_M2_SHIELD = register("hmmwv_unarmored_m2_shield",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_M2_TURRET = register("hmmwv_unarmored_m2_turret",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_TOW = register("hmmwv_unarmored_tow",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_TOW_TURRET = register("hmmwv_unarmored_tow_turret",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<HumveeUnarmedEntity>> HMMWV_UNARMORED_UNARMED = register("hmmwv_unarmored_unarmed",
            EntityType.Builder.of(HumveeUnarmedEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BTR3EEntity>> BTR3E = register("btr3e",
            EntityType.Builder.of(BTR3EEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BTR4MV1Entity>> BTR4MV1 = register("btr4mv1",
            EntityType.Builder.of(BTR4MV1Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BTR82Entity>> BTR82 = register("btr82",
            EntityType.Builder.of(BTR82Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BTR82ATEntity>> BTR82AT = register("btr82at",
            EntityType.Builder.of(BTR82ATEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BTR80Entity>> BTR80 = register("btr80",
            EntityType.Builder.of(BTR80Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BTR80CopeEntity>> BTR80_COPE = register("btr80_cope",
            EntityType.Builder.of(BTR80CopeEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<BTR82CopeEntity>> BTR82_COPE = register("btr82_cope",
            EntityType.Builder.of(BTR82CopeEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<FMTVEntity>> FMTV = register("fmtv",
            EntityType.Builder.of(FMTVEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    // Projectiles
    public static final RegistryObject<EntityType<LockOnHellfireEntity>> LOCK_ON_HELLFIRE = register("lock_on_hellfire",
            EntityType.Builder.<LockOnHellfireEntity>of(LockOnHellfireEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    public static final RegistryObject<EntityType<WireGuidedHellfireEntity>> WIRE_GUIDED_HELLFIRE = register("wire_guided_hellfire",
            EntityType.Builder.<WireGuidedHellfireEntity>of(WireGuidedHellfireEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    public static final RegistryObject<EntityType<SidewinderEntity>> SIDEWINDER = register("sidewinder",
            EntityType.Builder.<SidewinderEntity>of(SidewinderEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    public static final RegistryObject<EntityType<MalyutkaEntity>> MALYUTKA = register("malyutka",
            EntityType.Builder.<MalyutkaEntity>of(MalyutkaEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(false).setTrackingRange(256).setUpdateInterval(1).noSave().fireImmune().sized(0.5f, 0.5f));
    // Meme Vehicles
    public static final RegistryObject<EntityType<BigBirdEntity>> BIGBIRD = register("bigbird",
            EntityType.Builder.of(BigBirdEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f,2f));
    public static final RegistryObject<EntityType<WolfEntity>> T14_ARMATA = register("t14_armata",
            EntityType.Builder.of(WolfEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(0.5f,0.5f));
    public static final RegistryObject<EntityType<M109Entity>> M109 = register("m109",
            EntityType.Builder.of(M109Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<MI17Entity>> MI17 = register("mi17",
            EntityType.Builder.of(MI17Entity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(3f,2f));
    public static final RegistryObject<EntityType<JohnDeereEntity>> JOHN_DEERE = register("john_deere",
            EntityType.Builder.of(JohnDeereEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f, 3f));
    public static final RegistryObject<EntityType<CombineEntity>> COMBINE = register("combine",
            EntityType.Builder.of(CombineEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(2f, 3f));
    public static final RegistryObject<EntityType<SeederEntity>> SEEDER = register("seeder",
            EntityType.Builder.of(SeederEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(4f, 3f));
    public static final RegistryObject<EntityType<CultivatorEntity>> CULTIVATOR = register("cultivator",
            EntityType.Builder.of(CultivatorEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(4f, 3f));
    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> entityTypeBuilder) {
        return ENTITY_TYPES.register(name, () -> entityTypeBuilder.build(FCP.MODID + ":" + name));
    }

    // Trailers
    public static final RegistryObject<EntityType<ExampleTrailerEntity>> EXAMPLE_TRAILER = register("example_trailer",
            EntityType.Builder.of(ExampleTrailerEntity::new, MobCategory.MISC).setTrackingRange(512).setUpdateInterval(1).fireImmune().sized(5f, 3f));
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
