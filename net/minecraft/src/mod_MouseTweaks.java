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
		ModLoader.SetInGameHook(this, true, false);
	}

	@Override
	public boolean OnTickInGame(float time, Minecraft minecraft) {
		Main.onUpdateInGame();
		return true;
	}
}
