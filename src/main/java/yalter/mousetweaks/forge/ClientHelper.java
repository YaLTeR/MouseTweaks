package yalter.mousetweaks.forge;

import net.minecraftforge.client.ConfigGuiHandler;
import yalter.mousetweaks.ConfigScreen;

/**
 * Functions accessing client-only classes, extracted so that they can be called from MouseTweaksForge
 * without causing class-loading errors on the server.
 */
public class ClientHelper {
    public static ConfigGuiHandler.ConfigGuiFactory createConfigGuiFactory() {
        return new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen));
    }
}
