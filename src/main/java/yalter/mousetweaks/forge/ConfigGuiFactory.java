package yalter.mousetweaks.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.Logger;
import yalter.mousetweaks.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    public static class ConfigGui extends GuiConfig {
        private static Property RMBTweak = new Property("RMB tweak", "true", Property.Type.BOOLEAN);
        private static Property LMBTweakWithItem = new Property("LMB tweak with item", "true", Property.Type.BOOLEAN);
        private static Property LMBTweakWithoutItem = new Property("LMB tweak without item", "true", Property.Type.BOOLEAN);
        private static Property WheelTweak = new Property("Wheel tweak", "true", Property.Type.BOOLEAN);
        private static Property WheelSearchOrder = new Property("Wheel tweak search order", "Last to first", Property.Type.STRING, new String[] { "First to last", "Last to first" });
        private static Property OnTickMethodOrder = new Property("OnTick method order", "Forge, LiteLoader", Property.Type.STRING);
        private static Property Debug = new Property("Debug", "false", Property.Type.BOOLEAN);

        private boolean is_open = false;

        public ConfigGui(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), Constants.MOD_ID, false, false, ".minecraft/config/MouseTweaks.cfg");
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> list = new ArrayList<IConfigElement>();

            list.add(new ConfigElement(RMBTweak));
            list.add(new ConfigElement(LMBTweakWithItem));
            list.add(new ConfigElement(LMBTweakWithoutItem));
            list.add(new ConfigElement(WheelTweak));
            list.add(new ConfigElement(WheelSearchOrder));
            list.add(new ConfigElement(OnTickMethodOrder));
            list.add(new ConfigElement(Debug));

            return list;
        }

        @Override
        public void initGui() {
            Logger.DebugLog("initGui()");

            if (!is_open) {
                is_open = true;

                RMBTweak.set(Main.RMBTweak != 0);
                LMBTweakWithItem.set(Main.LMBTweakWithItem != 0);
                LMBTweakWithoutItem.set(Main.LMBTweakWithoutItem != 0);
                WheelTweak.set(Main.WheelTweak != 0);
                WheelSearchOrder.set((Main.WheelSearchOrder == 0) ? "First to last" : "Last to first");
                OnTickMethodOrder.set(Main.onTickMethodOrderToString());
                Debug.set(Main.Debug != 0);
            }

            super.initGui();
        }

        @Override
        public void onGuiClosed() {
            Logger.DebugLog("onGuiClosed()");

            Main.RMBTweak = RMBTweak.getBoolean() ? 1 : 0;
            Main.LMBTweakWithItem = LMBTweakWithItem.getBoolean() ? 1 : 0;
            Main.LMBTweakWithoutItem = LMBTweakWithoutItem.getBoolean() ? 1 : 0;
            Main.WheelTweak = WheelTweak.getBoolean() ? 1 : 0;
            Main.WheelSearchOrder = (WheelSearchOrder.getString().equals("First to last")) ? 0 : 1;
            Main.onTickMethodOrderFromString(OnTickMethodOrder.getString());
            Main.Debug = Debug.getBoolean() ? 1 : 0;
            Main.saveConfigFile();
            Main.findOnTickMethod(true);

            is_open = false;

            super.onGuiClosed();
        }
    }
}
