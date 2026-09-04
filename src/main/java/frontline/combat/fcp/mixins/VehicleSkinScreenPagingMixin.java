package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.client.screens.VehicleSkinScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Works around an off-by-one in SBW's skin screen that leaks one tile between pages.
 *
 * {@code VehicleSkinScreen.registerButtons} decides which skins belong on the current page with
 * an inclusive range over a 1-based index:
 *
 * <pre>
 *   start = currentPage * PAGE_SIZE          // PAGE_SIZE = 4
 *   end   = currentPage * PAGE_SIZE + 4
 *   n     = index + 1
 *   show if  start &lt;= n &amp;&amp; n &lt;= end          // admits FIVE items, not four
 * </pre>
 *
 * Page 0 is unaffected because n is never 0, but every later page also admits index
 * {@code currentPage * 4 - 1} - the last entry of the previous page. Tile position is
 * {@code slot = index % 4} laid out 2x2, and {@code (4p - 1) % 4 == 3}, so the leaked tile always
 * lands bottom-right. On a full page a real tile overdraws it; on a short final page it just sits
 * there as a duplicate.
 *
 * The leaked entry is always the FIRST one the loop admits, and the loop adds every skin tile
 * before the two page buttons, so dropping the first widget added on any page past the first is
 * enough. Upstream's one-character fix would be {@code start < n}.
 *
 * REMOVE THIS when SBW fixes the range - against a fixed upstream it would start discarding a
 * legitimate tile. Verified against SBW 0.8.9.1-hotfix (CurseForge file 8689629); recheck on every
 * SBW bump. require = 0 so a changed method signature degrades to the old duplicate rather than
 * preventing the mod from loading.
 */
@Mixin(value = VehicleSkinScreen.class, remap = false)
public abstract class VehicleSkinScreenPagingMixin extends Screen {

    protected VehicleSkinScreenPagingMixin(Component title) {
        super(title);
    }

    @Unique
    private boolean fcp$leakDropped;

    @Inject(method = "registerButtons", at = @At("HEAD"), remap = false, require = 0)
    private void fcp$resetLeakGuard(CallbackInfo ci) {
        fcp$leakDropped = false;
    }

    @Redirect(
            method = "registerButtons",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/client/screens/VehicleSkinScreen;"
                            + "addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)"
                            + "Lnet/minecraft/client/gui/components/events/GuiEventListener;",
                    remap = true
            ),
            remap = false,
            require = 0
    )
    private GuiEventListener fcp$dropLeakedTile(VehicleSkinScreen screen, GuiEventListener widget) {
        if (!fcp$leakDropped
                && ((VehicleSkinScreen) (Object) this).getCurrentPage() > 0
                && !"PageButton".equals(widget.getClass().getSimpleName())) {
            fcp$leakDropped = true;
            return widget; // swallowed: returned so the call site still gets a value, never added
        }
        // this is the screen instance at runtime; called on `this` because addRenderableWidget is
        // protected and only reachable through our own Screen supertype. It is generic over
        // GuiEventListener & Renderable & NarratableEntry, which AbstractWidget satisfies - every
        // widget this screen adds (SkinSlotButton, PageButton) is an AbstractButton.
        return this.addRenderableWidget((AbstractWidget) widget);
    }
}
