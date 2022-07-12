package yalter.mousetweaks.forge;

import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import yalter.mousetweaks.ConfigScreen;

/**
 * Functions accessing client-only classes, extracted so that they can be called from MouseTweaksForge
 * without causing class-loading errors on the server.
 */
public class ClientHelper {
    public static ConfigScreenFactory createConfigScreenFactory() {
        return new ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen));
    }
}
