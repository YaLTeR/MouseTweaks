package yalter.mousetweaks.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.MouseButton;

public class MouseTweaksFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Main.initialize();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            Main.onGuiOpen(screen);

            ScreenMouseEvents.allowMouseClick(screen).register((_screen, x, y, eventButton) -> {
                MouseButton button = MouseButton.fromEventButton(eventButton);
                if (button != null)
                    return !Main.onMouseClicked(x, y, button);
                return true;
            });

            ScreenMouseEvents.allowMouseRelease(screen).register((_screen, x, y, eventButton) -> {
                MouseButton button = MouseButton.fromEventButton(eventButton);
                if (button != null)
                    return !Main.onMouseReleased(x, y, button);
                return true;
            });

            ScreenMouseEvents.allowMouseScroll(screen).register((_screen, x, y, horiz, vert) -> !Main.onMouseScrolled(x, y, vert));
        });
    }
}
