package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.init.ModItems;
import frontline.combat.fcp.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class CamoVehicleBase extends GeoVehicleEntity implements ICamoVehicle {

    private static final EntityDataAccessor<Integer> CAMO_TYPE = SynchedEntityData.defineId(CamoVehicleBase.class, EntityDataSerializers.INT);

    public CamoVehicleBase(EntityType<? extends VehicleEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAMO_TYPE, 0);
    }

    /**
     * The texture array is split into two halves:
     * [0 .. camoCount-1] = normal camo textures
     * [camoCount .. total-1] = wrecked variants, one per camo in the same order
     *
     * camoCount = ceil(totalTextures / 2)
     *
     * When wrecked, the modifier (camoCount) is added to the current camo index
     * to land on the corresponding wrecked texture.
     */
    public ResourceLocation getCurrentTexture() {
        ResourceLocation[] textures = getCamoTextures();
        int total = textures.length;
        // Round up so an odd total always favours the camo side
        int camoCount = (int) Math.ceil(total / 2.0);
        int index = getCamoType();
        if (index < 0 || index >= camoCount) index = 0;

        if (this.isWreck()) {
            int wreckedIndex = index + camoCount;
            // Safety clamp in case of an odd total leaving one fewer wrecked texture
            if (wreckedIndex >= total) wreckedIndex = total - 1;
            return textures[wreckedIndex];
        }

        return textures[index];
    }

    @Override
    public int getCamoType() {
        return this.entityData.get(CAMO_TYPE);
    }

    @Override
    public void setCamoType(int camoType) {
        this.entityData.set(CAMO_TYPE, camoType);
    }

    @Override
    public void cycleCamo() {
        int total = getCamoTextures().length;
        int camoCount = (int) Math.ceil(total / 2.0);
        int current = getCamoType();
        // Only cycle within the normal camo range, never into the wrecked half
        setCamoType((current + 1) % camoCount);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).is(ModItems.SPRAY.get())) {
            if (!this.level().isClientSide) {
                cycleCamo();
                String[] camoNames = getCamoNames();
                int camoType = getCamoType();
                String camoName = (camoType >= 0 && camoType < camoNames.length)
                        ? camoNames[camoType]
                        : "Unknown";

                player.displayClientMessage(
                        Component.translatable("message.fcp.camo_changed", camoName).withStyle(ChatFormatting.GREEN),
                        true
                );
                this.level().playSound(null, this.blockPosition(),
                        ModSounds.SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("CamoType", getCamoType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("CamoType")) {
            setCamoType(compound.getInt("CamoType"));
        }
    }

    @Override
    public abstract ResourceLocation[] getCamoTextures();

    @Override
    public abstract String[] getCamoNames();
}