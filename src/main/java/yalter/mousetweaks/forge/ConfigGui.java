package yalter.mousetweaks.forge;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import yalter.mousetweaks.*;

import java.util.*;

public class ConfigGui extends GuiConfig {
	private static Property rmbTweak = booleanProperty("RMB tweak", true);
	private static Property lmbTweakWithItem = booleanProperty("LMB tweak with item", true);
	private static Property lmbTweakWithoutItem = booleanProperty("LMB tweak without item", true);
	private static Property wheelTweak = booleanProperty("Wheel tweak", true);
	private static Property wheelSearchOrder = choiceProperty("Wheel tweak search order", "Last to first", "First to last");
	private static Property wheelScrollDirection = choiceProperty(
		"Wheel tweak scroll direction",
		"Down to push, up to pull",
		"Up to push, down to pull",
		"Inventory position aware",
		"Inventory position aware, inverted"
	);
	private static Property onTickMethodOrder = new Property(
		"OnTick method order",
		"Forge, LiteLoader",
		Property.Type.STRING
	);
	private static Property scrollHandling = choiceProperty(
		"Scroll handling",
		"Smooth scrolling, minor issues",
		"Non-smooth scrolling, no issues"
	);
	private static Property scrollItemScaling = choiceProperty(
		"Scroll item scaling",
		"Relative to scroll amount",
		"Always exactly one item"
	);
	private static Property debug = booleanProperty("Debug", false);
	
	private static Property booleanProperty(String name, boolean defaultValue) {
		return new Property(name, Boolean.toString(defaultValue), Property.Type.BOOLEAN);
	}
	
	/** creates a new string property with the given valid values, taking the first valid value as default */
	private static Property choiceProperty(String name, String... values) {
		return new Property(name, values[0], Property.Type.STRING, values);
	}
	
	private boolean is_open = false;
	
	public ConfigGui(GuiScreen parentScreen) {
		super(parentScreen, getConfigElements(), Constants.MOD_ID, false, false, ".minecraft/config/MouseTweaks.cfg");
		
		rmbTweak.setComment("Like vanilla right click dragging, but dragging over a slot multiple times puts the item there multiple times.");
		lmbTweakWithItem.setComment("Left click and drag with an item to \"left click\" items of the same type.");
		lmbTweakWithoutItem.setComment("Hold shift, left click and drag without an item to \"shift left click\" items.");
		wheelTweak.setComment("Scroll over items to move them between inventories.");
		wheelSearchOrder.setComment("How to pick the source slot when pulling items via scrolling.");
		wheelScrollDirection.setComment("Inventory position aware means scroll up to push items from the bottom inventory and pull into the top inventory, and vice versa.");
		onTickMethodOrder.setComment("This shouldn't really affect anything, but non-smooth scrolling works only with the Forge OnTick method.");
		scrollHandling.setComment("When set to smooth scrolling, minor issues may be experienced such as scrolling \"through\" JEI or other mods. Non-smooth scrolling works only with the Forge OnTick method.");
		scrollItemScaling.setComment("This determines how many items are moved when you scroll. On some setups (notably macOS), scrolling the wheel with different speeds results in different distances scrolled per wheel \"bump\". To make those setups play nicely with Mouse Tweaks, set this option to \"Always exactly one item\".");
		debug.setComment("Enables debug logging output.");
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
		list.add(new ConfigElement(scrollHandling));
		list.add(new ConfigElement(scrollItemScaling));
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
			wheelSearchOrder.set((Main.config.wheelSearchOrder == WheelSearchOrder.FIRST_TO_LAST)
				? "First to last"
				: "Last to first");
			wheelScrollDirection.set(wheelScrollDirection.getValidValues()[Main.config.wheelScrollDirection.ordinal()]);
			onTickMethodOrder.set(Main.config.onTickMethodOrderString());
			scrollHandling.set(scrollHandling.getValidValues()[Main.config.scrollHandling.ordinal()]);
			scrollItemScaling.set(scrollItemScaling.getValidValues()[Main.config.scrollItemScaling.ordinal()]);
			debug.set(Config.debug);
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
		Main.config.wheelSearchOrder = wheelSearchOrder.getString().equals("First to last")
			? WheelSearchOrder.FIRST_TO_LAST
			: WheelSearchOrder.LAST_TO_FIRST;
		Main.config.wheelScrollDirection = WheelScrollDirection.fromId(Arrays.asList(wheelScrollDirection.getValidValues())
			.indexOf(wheelScrollDirection.getString()));
		Main.config.onTickMethodOrderFromString(onTickMethodOrder.getString());
		Main.config.scrollHandling = ScrollHandling.fromId(Arrays.asList(scrollHandling.getValidValues())
			.indexOf(scrollHandling.getString()));
		Main.config.scrollItemScaling = ScrollItemScaling.fromId(Arrays.asList(scrollItemScaling.getValidValues())
			.indexOf(scrollItemScaling.getString()));
		Config.debug = debug.getBoolean();
		Main.config.save();
		Main.findOnTickMethod(true);
		
		is_open = false;
		super.onGuiClosed();
	}
}
