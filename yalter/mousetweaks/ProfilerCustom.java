package yalter.mousetweaks;

import net.minecraft.src.Minecraft;
import net.minecraft.src.Profiler;

public class ProfilerCustom extends Profiler
{
    private Minecraft minecraft;
    
    public ProfilerCustom()
    {
        minecraft = Minecraft.getMinecraft();
    }
    
    @Override
    public void startSection( String sectionName )
    {
        if ( "gameRenderer".equals( sectionName ) )
        {
            Main.onUpdateInGame();
        }
        
        super.startSection( sectionName );
        
        if ( Main.optifine )
        {
            if ( !Reflection.gameSettings.setFieldValue( minecraft.gameSettings, "ofProfiler", true ) )
            {
                Main.optifine = false;
            }
        }
    }
    
    @Override
    public void endSection()
    {
        super.endSection();
    }
}
