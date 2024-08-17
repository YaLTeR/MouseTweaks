package yalter.mousetweaks.neoforge;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import yalter.mousetweaks.ConfigScreen;

/**
 * Functions accessing client-only classes, extracted so that they can be called from MouseTweaksNeo
 * without causing class-loading errors on the server.
 */
public class ClientHelper implements IConfigScreenFactory {
    @Override
    public Screen createScreen(ModContainer container, Screen modListScreen) {
        return new ConfigScreen(modListScreen);
    }
}
