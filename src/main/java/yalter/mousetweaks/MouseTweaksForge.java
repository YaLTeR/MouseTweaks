package yalter.mousetweaks;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

@Mod(modid = "MouseTweaks", useMetadata = true)
public class MouseTweaksForge {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        Main.initialise();

        if (Main.useForge)
        {
            FMLCommonHandler.instance().bus().register(this);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            Main.onUpdateInGame();
        }
    }
}
