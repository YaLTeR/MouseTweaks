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
import yalter.mousetweaks.WheelScrollDirection;
import yalter.mousetweaks.WheelSearchOrder;

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
        private static Property rmbTweak = new Property("RMB tweak", "true", Property.Type.BOOLEAN);
        private static Property lmbTweakWithItem = new Property("LMB tweak with item", "true", Property.Type.BOOLEAN);
        private static Property lmbTweakWithoutItem = new Property("LMB tweak without item", "true", Property.Type.BOOLEAN);
        private static Property wheelTweak = new Property("Wheel tweak", "true", Property.Type.BOOLEAN);
        private static Property wheelSearchOrder = new Property("Wheel tweak search order", "Last to first", Property.Type.STRING, new String[] { "First to last", "Last to first" });
        private static Property wheelScrollDirection = new Property("Wheel tweak scroll direction", "Down to push, up to pull", Property.Type.STRING, new String[] { "Down to push, up to pull", "Up to push, down to pull" });
        private static Property onTickMethodOrder = new Property("OnTick method order", "Forge, LiteLoader", Property.Type.STRING);
        private static Property debug = new Property("Debug", "false", Property.Type.BOOLEAN);

        private boolean is_open = false;

        public ConfigGui(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), Constants.MOD_ID, false, false, ".minecraft/config/MouseTweaks.cfg");
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> list = new ArrayList<IConfigElement>();

            list.add(new ConfigElement(rmbTweak));
            list.add(new ConfigElement(lmbTweakWithItem));
            list.add(new ConfigElement(lmbTweakWithoutItem));
            list.add(new ConfigElement(wheelTweak));
            list.add(new ConfigElement(wheelSearchOrder));
            list.add(new ConfigElement(wheelScrollDirection));
            list.add(new ConfigElement(onTickMethodOrder));
            list.add(new ConfigElement(debug));

            return list;
        }

        @Override
        public void initGui() {
            Logger.DebugLog("initGui()");

            if (!is_open) {
                is_open = true;

                Main.config.read();
                rmbTweak.set(Main.config.rmbTweak);
                lmbTweakWithItem.set(Main.config.lmbTweakWithItem);
                lmbTweakWithoutItem.set(Main.config.lmbTweakWithoutItem);
                wheelTweak.set(Main.config.wheelTweak);
                wheelSearchOrder.set(
                    (Main.config.wheelSearchOrder == WheelSearchOrder.FIRST_TO_LAST)
                        ? "First to last"
                        : "Last to first");
                wheelScrollDirection.set(
                    (Main.config.wheelScrollDirection == WheelScrollDirection.NORMAL)
                        ? "Down to push, up to pull"
                        : "Up to push, down to pull");
                onTickMethodOrder.set(Main.config.onTickMethodOrderString());
                debug.set(Main.config.debug);
            }

            super.initGui();
        }

        @Override
        public void onGuiClosed() {
            Logger.DebugLog("onGuiClosed()");

            Main.config.rmbTweak = rmbTweak.getBoolean();
            Main.config.lmbTweakWithItem = lmbTweakWithItem.getBoolean();
            Main.config.lmbTweakWithoutItem = lmbTweakWithoutItem.getBoolean();
            Main.config.wheelTweak = wheelTweak.getBoolean();
            Main.config.wheelSearchOrder =
                wheelSearchOrder.getString().equals("First to last")
                    ? WheelSearchOrder.FIRST_TO_LAST
                    : WheelSearchOrder.LAST_TO_FIRST;
            Main.config.wheelScrollDirection =
                wheelScrollDirection.getString().equals("Down to push, up to pull")
                    ? WheelScrollDirection.NORMAL
                    : WheelScrollDirection.INVERTED;
            Main.config.onTickMethodOrderFromString(onTickMethodOrder.getString());
            Main.config.debug = debug.getBoolean();
            Main.config.save();
            Main.findOnTickMethod(true);

            is_open = false;
            super.onGuiClosed();
        }
    }
}
