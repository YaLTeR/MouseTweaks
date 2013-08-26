package MouseTweaks;

import java.io.File;

import net.minecraft.src.GuiScreen;

import com.mumfrey.liteloader.RenderListener;

public class LiteModMouseTweaks implements RenderListener
{
    
    private static LiteModMouseTweaks instance;
    
    public static LiteModMouseTweaks getInstance()
    {
        if ( instance == null )
        {
            instance = new LiteModMouseTweaks();
        }
        
        return instance;
    }
    
    @Override
    public String getName()
    {
        return Constants.NAME;
    }
    
    @Override
    public String getVersion()
    {
        return Constants.VERSION;
    }
    
    @Override
    public void onRender()
    {
        Main.onUpdateInGame();
    }
    
    @Override
    public void onRenderGui( GuiScreen currentScreen )
    {
    }
    
    @Override
    public void onRenderWorld()
    {
    }
    
    @Override
    public void onSetupCameraTransform()
    {
    }
    
    @Override
    public void init()
    {
        Main.initialise();
    }
}
