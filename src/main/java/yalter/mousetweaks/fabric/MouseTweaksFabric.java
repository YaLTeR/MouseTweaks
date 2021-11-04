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

        // We need BEFORE_INIT rather than AFTER_INIT to get the correct ordering on the creative inventory.
        // The game always opens InventoryScreen, calling its init() first, then if the player is in creative mode,
        // InventoryScreen sets the screen to CreativeInventoryScreen within its init(). Thus, when using AFTER_INIT,
        // CreativeInventoryScreen is called first, but then InventoryScreen is called right after as its init() exits.
        // This breaks special creative inventory handling.
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
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
