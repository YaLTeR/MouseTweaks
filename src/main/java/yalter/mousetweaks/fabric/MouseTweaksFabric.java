package yalter.mousetweaks.fabric;

import net.fabricmc.api.ClientModInitializer;
import yalter.mousetweaks.Main;

public class MouseTweaksFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Main.initialize();
    }
}
