package net.minecraft.src;

import net.minecraft.client.Minecraft;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Main;

public class mod_MouseTweaks extends BaseMod {
	
	public String Name() {
		return Constants.MOD_NAME;
	}
	
	public String Description() {
		return Constants.MOD_DESC;
	}
	
	public String Icon() {
		return Constants.ICON;
	}

	public mod_MouseTweaks() {
		Main.initialize(Constants.EntryPoint.FORGE);
		ModLoader.SetInGameHook(this, true, false);
	}

	@Override
	public boolean OnTickInGame(Minecraft minecraft) {
		Main.onUpdateInGame();
		return true;
	}

	@Override
	public String Version() {
		return Constants.VERSION;
	}
}
