package yalter.mousetweaks.neoforge;

import net.neoforged.neoforge.client.ConfigScreenHandler;
import yalter.mousetweaks.ConfigScreen;

/**
 * Functions accessing client-only classes, extracted so that they can be called from MouseTweaksNeo
 * without causing class-loading errors on the server.
 */
public class ClientHelper {
    public static ConfigScreenHandler.ConfigScreenFactory createConfigScreenFactory() {
        return new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen));
    }
}
