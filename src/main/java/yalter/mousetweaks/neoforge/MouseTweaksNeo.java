package yalter.mousetweaks.neoforge;

import net.neoforged.neoforge.client.event.ScreenEvent.MouseButtonPressed;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseButtonReleased;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseDragged;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseScrolled;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.Mod;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Logger;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.MouseButton;

@Mod(Constants.MOD_ID)
public class MouseTweaksNeo {
    public MouseTweaksNeo() {
        if (FMLEnvironment.dist != net.neoforged.api.distmarker.Dist.CLIENT) {
            Logger.Log("Disabled because not running on the client.");
            return;
        }

        Main.initialize();
        NeoForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, ClientHelper::new);
    }

    @SubscribeEvent
    public void onGuiMouseClickedPre(MouseButtonPressed.Pre event) {
        Logger.DebugLog("onGuiMouseClickedPre button = " + event.getButton());

        MouseButton button = MouseButton.fromEventButton(event.getButton());
        if (button != null) {
            if (Main.onMouseClicked(event.getScreen(), event.getMouseX(), event.getMouseY(), button))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiMouseReleasedPre(MouseButtonReleased.Pre event) {
        Logger.DebugLog("onGuiMouseReleasedPre button = " + event.getButton());

        MouseButton button = MouseButton.fromEventButton(event.getButton());
        if (button != null) {
            if (Main.onMouseReleased(event.getScreen(), event.getMouseX(), event.getMouseY(), button))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiMouseScrollPost(MouseScrolled.Post event) {
        // Sent when nothing handled the scroll itself. For example, the creative inventory handles scroll anywhere on
        // screen, so this event is suppressed. Quick scrolls at limited FPS result in multiple scroll events rather
        // than one with a bigger delta.
        Logger.DebugLog("onGuiMouseScrollPost delta = " + event.getScrollDeltaY());

        // Post events aren't cancellable, but that's okay.
        Main.onMouseScrolled(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getScrollDeltaY());
    }

    @SubscribeEvent
    public void onGuiMouseDragPre(MouseDragged.Pre event) {
        // Sent when a mouse is dragged while a mouse button is down (so between Clicked and Released events). The
        // rate of reporting is high even when the FPS is limited through the options.
        Logger.DebugLog("onGuiMouseDragPre button = " + event.getMouseButton() + ", dx = " + event.getDragX() + ", dy = " + event.getDragY());

        MouseButton button = MouseButton.fromEventButton(event.getMouseButton());
        if (button != null) {
            if (Main.onMouseDrag(event.getScreen(), event.getMouseX(), event.getMouseY(), button))
                event.setCanceled(true);
        }
    }
}
