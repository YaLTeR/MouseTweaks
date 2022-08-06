package net.minecraft.src;

import net.minecraft.client.Minecraft;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Main;

public class mod_MouseTweaks extends BaseMod {

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
