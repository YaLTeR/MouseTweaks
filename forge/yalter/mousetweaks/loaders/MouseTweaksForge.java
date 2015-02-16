package yalter.mousetweaks.loaders;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Main;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.MOD_VERSION, useMetadata = true)
public class MouseTweaksForge {
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		Main.initialise(Constants.EntryPoint.FORGE);

		if (Main.useForge)
			FMLCommonHandler.instance().bus().register(this);
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		// The useForge check is redundant here since the OnTick method cannot change in the runtime (at the time of writing this comment).
		if (Main.useForge && event.phase == TickEvent.Phase.START)
			Main.onUpdateInGame();
	}
}
