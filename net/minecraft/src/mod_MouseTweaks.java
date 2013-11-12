package net.minecraft.src;

import net.minecraft.client.Minecraft;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Main;

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
