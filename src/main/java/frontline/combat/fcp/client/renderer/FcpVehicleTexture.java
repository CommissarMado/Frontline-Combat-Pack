package frontline.combat.fcp.client.renderer;

import com.atsuishio.superbwarfare.client.renderer.SmartTextureBrightener;
import com.atsuishio.superbwarfare.client.renderer.TextureBrightnessHandler;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import net.minecraft.resources.ResourceLocation;

/**
 * Applies SBW's texture-level vehicle states (thermal imaging, wreck) to an FCP camo skin.
 *
 * SBW does this inside {@code VehicleRenderer.getTextureLocation}, deriving both states from the
 * base skin at runtime. Every FCP vehicle renderer overrides that method to return its camo skin,
 * so without routing through here the states are silently dropped - which is why FCP vehicles were
 * invisible to thermal optics (they rendered flat and the post-process merely vignetted them).
 *
 * Both SBW brighteners memoise their output ({@code PROCESSED_TEXTURES} / {@code
 * BRIGHTENED_TEXTURES}), so each distinct skin is processed once per session, not per frame.
 *
 * Deviation from SBW: SBW only darkens Airplane/Helicopter/Airship wrecks when they
 * sympathetically detonated, leaving other aircraft wrecks with an undamaged skin. FCP darkens
 * every wreck regardless of type, so its 10 helicopters keep reading as wrecked when downed.
 */
public final class FcpVehicleTexture {

    /** Matches SBW's thermal brightening factor. */
    private static final float THERMAL_BRIGHTNESS = 3.0f;

    /** Matches SBW's wreck darkening factor. */
    private static final float WRECK_BRIGHTNESS = 0.3f;

    private FcpVehicleTexture() {
    }

    /**
     * @param entity  the vehicle being rendered
     * @param texture its normal camo skin, usually {@code entity.getCurrentTexture()}
     * @return the skin adjusted for thermal imaging or wreck state, else {@code texture} unchanged
     */
    public static ResourceLocation resolve(VehicleEntity entity, ResourceLocation texture) {
        if (ClientEventHandler.activeThermalImaging) {
            return SmartTextureBrightener.getSmartBrightenedTexture(texture, THERMAL_BRIGHTNESS);
        }
        if (entity.isWreck()) {
            return TextureBrightnessHandler.INSTANCE.getBrightenedTexture(texture, WRECK_BRIGHTNESS);
        }
        return texture;
    }
}
