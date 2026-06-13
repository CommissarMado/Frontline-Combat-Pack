package frontline.combat.fcp.client.overlay;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraft.world.entity.EntityType;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class FcpDriverOverlay implements IGuiOverlay {

    public static final FcpDriverOverlay INSTANCE = new FcpDriverOverlay();
    public static final String ID = "fcp_driver_hud";

    private static final ResourceLocation DRIVER_FRAME =
            new ResourceLocation("fcp", "textures/overlay/vehicle/frame/driver.png");

    private static final Set<String> DRIVER_OVERLAY_VEHICLES = Set.of(
            "fcp:bmp1",
            "fcp:bmp2",
            "fcp:btr82",
            "fcp:lav25",
            "fcp:stryker_m2",
            "fcp:stryker_mgs",
            "fcp:t72av"
    );

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick,
                       int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator() || mc.options.hideGui) return;
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return;

        var vehicle = player.getVehicle();
        if (!(vehicle instanceof VehicleEntity ve)) return;
        if (!ve.computed().getHudType().equals("@Land")) return;

        int seatIndex = ve.getSeatIndex(player);
        // Only the driver (seat 0), and only if they're NOT also the turret controller
        if (seatIndex != 0) return;
        if (seatIndex == ve.computed().getTurretControllerIndex()) return;

        String vehicleId = EntityType.getKey(ve.getType()).toString();
        if (!DRIVER_OVERLAY_VEHICLES.contains(vehicleId)) return;

        float addW = ((float) screenWidth / screenHeight) * 48;
        float addH = ((float) screenWidth / screenHeight) * 27;

        RenderHelper.preciseBlit(
                guiGraphics,
                DRIVER_FRAME,
                -addW / 2, -addH / 2,
                10f, 0f, 0f,
                screenWidth + addW, screenHeight + addH,
                screenWidth + addW, screenHeight + addH
        );
    }
}