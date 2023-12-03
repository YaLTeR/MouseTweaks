package yalter.mousetweaks.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ScreenEvent.MouseClickedEvent;
import net.minecraftforge.client.event.ScreenEvent.MouseDragEvent;
import net.minecraftforge.client.event.ScreenEvent.MouseReleasedEvent;
import net.minecraftforge.client.event.ScreenEvent.MouseScrollEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Logger;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.MouseButton;

@Mod(Constants.MOD_ID)
public class MouseTweaksForge {
    public MouseTweaksForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
        if (FMLEnvironment.dist != Dist.CLIENT) {
            Logger.Log("Disabled because not running on the client.");
            return;
        }

        if (FMLLoader.getLaunchHandler().isData()) {
            Logger.Log("Disabled because running data generation.");
            return;
        }

        Main.initialize();
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, ClientHelper::createConfigGuiFactory);
    }

    @SubscribeEvent
    public void onGuiMouseClickedPre(MouseClickedEvent.Pre event) {
        Logger.DebugLog("onGuiMouseClickedPre button = " + event.getButton());

        MouseButton button = MouseButton.fromEventButton(event.getButton());
        if (button != null) {
            if (Main.onMouseClicked(event.getScreen(), event.getMouseX(), event.getMouseY(), button))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiMouseReleasedPre(MouseReleasedEvent.Pre event) {
        Logger.DebugLog("onGuiMouseReleasedPre button = " + event.getButton());

        MouseButton button = MouseButton.fromEventButton(event.getButton());
        if (button != null) {
            if (Main.onMouseReleased(event.getScreen(), event.getMouseX(), event.getMouseY(), button))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiMouseScrollPost(MouseScrollEvent.Post event) {
        // Sent when nothing handled the scroll itself. For example, the creative inventory handles scroll anywhere on
        // screen, so this event is suppressed. Quick scrolls at limited FPS result in multiple scroll events rather
        // than one with a bigger delta.
        Logger.DebugLog("onGuiMouseScrollPost delta = " + event.getScrollDelta());

        if (Main.onMouseScrolled(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getScrollDelta()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onGuiMouseDragPre(MouseDragEvent.Pre event) {
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
