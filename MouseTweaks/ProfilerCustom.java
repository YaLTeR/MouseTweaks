package MouseTweaks;

import java.lang.reflect.Field;
import java.util.LinkedList;

import net.minecraft.src.Minecraft;
import net.minecraft.src.GameSettings;
import net.minecraft.src.Profiler;

public class ProfilerCustom extends Profiler
{
    private LinkedList<String> sectionStack = new LinkedList<String>();
    private Minecraft minecraft;
    
    public ProfilerCustom()
    {
        minecraft = Minecraft.getMinecraft();
    }
    
    @Override
    public void startSection(String sectionName)
    {
        if ("gameRenderer".equals(sectionName) && "root".equals(sectionStack.getLast())) {
            Main.onUpdateInGame();
        }
        
        this.sectionStack.add(sectionName);
        super.startSection(sectionName);

        if (Main.optifine) {
            if (!Reflection.gameSettings.setFieldValue(minecraft.gameSettings, "ofProfiler", true)) {
                Main.optifine = false;
            }
        }
    }
    
    @Override
    public void endSection() {
        super.endSection();
        
        if (sectionStack.size() > 0) {
            sectionStack.removeLast();
        }
    }
}
