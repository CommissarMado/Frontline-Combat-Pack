package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.item.container.ContainerBlockItem;
import frontline.combat.fcp.FCP;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves the creative-tab icon for an SBW container stack that holds an FCP vehicle.
 * The PNG lives at superbwarfare:textures/vehicle_icon/container/&lt;id&gt;.png (shipped by FCP).
 * Scoped to fcp: vehicles only so SBW's own containers are left untouched.
 *
 * This is FCP's own mechanism; it does not rely on SBW's container-icon feature being present.
 */
public final class FcpContainerIcon {
    private FcpContainerIcon() {}

    // Cache successful lookups only; misses are re-checked so F3+T / newly added icons are picked up.
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    public static ResourceLocation getIcon(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ContainerBlockItem)) return null;
        CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag == null || !tag.contains("EntityType")) return null;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("EntityType"));
        if (id == null || !FCP.MODID.equals(id.getNamespace())) return null; // FCP vehicles only

        String key = id.getPath();
        ResourceLocation hit = CACHE.get(key);
        if (hit != null) return hit;

        ResourceLocation path = new ResourceLocation("superbwarfare", "textures/vehicle_icon/container/" + key + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(path).isPresent()) {
            CACHE.put(key, path);
            return path;
        }
        return null;
    }
}
