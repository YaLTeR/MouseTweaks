package yalter.mousetweaks.forge;

import net.minecraftforge.client.event.ScreenEvent.MouseButtonPressed;
import net.minecraftforge.client.event.ScreenEvent.MouseButtonReleased;
import net.minecraftforge.client.event.ScreenEvent.MouseDragged;
import net.minecraftforge.client.event.ScreenEvent.MouseScrolled;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yalter.mousetweaks.*;

import java.lang.invoke.MethodHandles;

@Mod(Constants.MOD_ID)
public class MouseTweaksForge {
    public MouseTweaksForge() {
        Main.initialize();
        BusGroup.DEFAULT.register(MethodHandles.lookup(), this);

        MinecraftForge.registerConfigScreen(ConfigScreen::new);
    }

    @SubscribeEvent
    public boolean onGuiMouseClickedPre(MouseButtonPressed.Pre event) {
        Logger.DebugLog("onGuiMouseClickedPre button = " + event.getButton());

        MouseButton button = MouseButton.fromEventButton(event.getButton());
        if (button != null) {
            if (Main.onMouseClicked(event.getScreen(), event.getMouseX(), event.getMouseY(), button))
                return true;
        }

        return false;
    }

    @SubscribeEvent
    public boolean onGuiMouseReleasedPre(MouseButtonReleased.Pre event) {
        Logger.DebugLog("onGuiMouseReleasedPre button = " + event.getButton());

        MouseButton button = MouseButton.fromEventButton(event.getButton());
        if (button != null) {
            if (Main.onMouseReleased(event.getScreen(), event.getMouseX(), event.getMouseY(), button))
                return true;
        }

        return false;
    }

    @SubscribeEvent
    public void onGuiMouseScrollPost(MouseScrolled.Post event) {
        // Sent when nothing handled the scroll itself. For example, the creative inventory handles scroll anywhere on
        // screen, so this event is suppressed. Quick scrolls at limited FPS result in multiple scroll events rather
        // than one with a bigger delta.
        Logger.DebugLog("onGuiMouseScrollPost delta = " + event.getDeltaY());

        // Post events aren't cancellable, but that's okay.
        Main.onMouseScrolled(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getDeltaY());
    }

    @SubscribeEvent
    public boolean onGuiMouseDragPre(MouseDragged.Pre event) {
        // Sent when a mouse is dragged while a mouse button is down (so between Clicked and Released events). The
        // rate of reporting is high even when the FPS is limited through the options.
        Logger.DebugLog("onGuiMouseDragPre button = " + event.getMouseButton() + ", dx = " + event.getDragX() + ", dy = " + event.getDragY());

        MouseButton button = MouseButton.fromEventButton(event.getMouseButton());
        if (button != null) {
            if (Main.onMouseDrag(event.getScreen(), event.getMouseX(), event.getMouseY(), button))
                return true;
        }

        return false;
    }
}
