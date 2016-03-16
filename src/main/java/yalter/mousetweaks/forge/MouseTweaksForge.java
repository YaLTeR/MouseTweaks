package yalter.mousetweaks.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.OnTickMethod;

@Mod(
	modid = Constants.MOD_ID, // If this isn't here, the mod doesn't load.
	version = Constants.VERSION, // If this isn't here, FML complains in the log.
	updateJSON = Constants.UPDATE_URL, // If this isn't here, updating doesn't work.
	useMetadata = true, // The rest of stuff is fine being exclusively in mcmod.info.
	clientSideOnly = true,
	guiFactory = "yalter.mousetweaks.forge.ConfigGuiFactory")
public class MouseTweaksForge {
	@EventHandler
	public void init(FMLInitializationEvent event) {
		Main.initialize(Constants.EntryPoint.FORGE);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (Main.onTickMethod == OnTickMethod.FORGE && event.phase == TickEvent.Phase.START)
			Main.onUpdateInGame();
	}
}
