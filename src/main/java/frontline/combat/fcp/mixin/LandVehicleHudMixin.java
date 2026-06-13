package frontline.combat.fcp.mixin;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.client.overlay.weapon.LandVehicleHud;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LandVehicleHud.class, remap = false)
public class LandVehicleHudMixin {

    private static final ResourceLocation DRIVER_FRAME =
            new ResourceLocation("fcp", "textures/overlay/vehicle/frame/driver.png");
    private static final ResourceLocation GUNNER_FRAME =
            new ResourceLocation("fcp", "textures/overlay/vehicle/frame/gunner.png");

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/client/RenderHelper;preciseBlit(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;FFFFFFFFFF)V",
                    ordinal = 0,
                    remap = false
            ),
            cancellable = true,
            remap = false
    )
    private static void replaceTvFrame(
            VehicleEntity vehicle,
            Player player,
            ForgeGui gui,
            GuiGraphics guiGraphics,
            float partialTick,
            int screenWidth,
            int screenHeight,
            CallbackInfo ci
    ) {
        // Only intercept for FCP vehicles
        var registryName = EntityType.getKey(vehicle.getType());
        if (registryName == null || !registryName.getNamespace().equals("fcp")) return;

        int seatIndex = vehicle.getSeatIndex(player);
        int turretIndex = vehicle.computed().getTurretControllerIndex();

        ResourceLocation frame;
        if (seatIndex == turretIndex) {
            frame = GUNNER_FRAME;
        } else if (player == vehicle.getFirstPassenger()) {
            frame = DRIVER_FRAME;
        } else {
            return; // other seats: let the default tv_frame through (or just cancel with no draw)
        }

        ci.cancel();

        float addW = ((float) screenWidth / screenHeight) * 48;
        float addH = ((float) screenWidth / screenHeight) * 27;

        RenderHelper.preciseBlit(
                guiGraphics,
                frame,
                -addW / 2,
                -addH / 2,
                10f,
                0f, 0f,
                screenWidth + addW,
                screenHeight + addH,
                screenWidth + addW,
                screenHeight + addH
        );
    }
}