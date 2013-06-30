package net.minecraft.src;

import net.minecraft.client.Minecraft;
import MouseTweaks.Constants;
import MouseTweaks.Logger;
import MouseTweaks.Main;
import MouseTweaks.Reflection;

public class mod_MouseTweaks extends BaseMod {

	@Override
	public String getName() {
		return Constants.NAME;
	}
	
	@Override
	public String getVersion() {
		return Constants.VERSION;
	}

	@Override
	public void load() {
	    if (!Main.initialise())
	        return;
	    
	    if (!Main.useModLoader)
	        return;
	    
	    ModLoader.setInGameHook(this, true, true);
    }
    
    @Override
    public boolean onTickInGame(float clock, Minecraft minecraft) {
        Main.onUpdateInGame();
        return true;
    }

}
