package yalter.mousetweaks;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.init.Blocks;

@Mod(modid = MouseTweaksForge.MODID, version = MouseTweaksForge.VERSION, useMetadata = true)
public class MouseTweaksForge {
    public static final String MODID = "MouseTweaks";
    public static final String VERSION = Constants.VERSION;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        Main.initialise();
    }
}
