package yalter.mousetweaks.liteloader;

import com.mumfrey.liteloader.RenderListener;
import net.minecraft.client.gui.GuiScreen;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.OnTickMethod;

import java.io.File;

public class LiteModMouseTweaks implements RenderListener {
    private static LiteModMouseTweaks instance;

    public static LiteModMouseTweaks getInstance() {
        if (instance == null)
            instance = new LiteModMouseTweaks();

        return instance;
    }

    public LiteModMouseTweaks() { }

    @Override
    public void init(File file) {
        Main.initialize(Constants.EntryPoint.LITELOADER);
    }

    @Override
    public void onRender() {
        if (Main.onTickMethod == OnTickMethod.LITELOADER)
            Main.onUpdateInGame();
    }

    @Override
    public String getName() {
        return Constants.MOD_NAME;
    }

    @Override
    public String getVersion() {
        return Constants.VERSION;
    }

    @Override
    public void onRenderGui(GuiScreen guiScreen) { }

    @Override
    public void onSetupCameraTransform() { }

    @Override
    public void upgradeSettings(String s, File file, File file2) { }
}
