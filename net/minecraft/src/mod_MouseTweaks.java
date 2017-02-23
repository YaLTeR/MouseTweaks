package net.minecraft.src;

import net.minecraft.client.Minecraft;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Main;

public class mod_MouseTweaks extends BaseMod {
	@Override
	public String getName() {
		return Constants.MOD_NAME;
	}

	@Override
	public String getVersion() {
		return Constants.VERSION;
	}

	@Override
	public void load() {
		Main.initialize(Constants.EntryPoint.FORGE);
		ModLoader.setInGameHook(this, true, false);
	}

	@Override
	public boolean onTickInGame(float time, Minecraft minecraft) {
		Main.onUpdateInGame();
		return true;
	}
}
