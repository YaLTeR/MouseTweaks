package yalter.mousetweaks.forge;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import yalter.mousetweaks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigGui extends GuiConfig {
	private static Property rmbTweak = new Property("RMB tweak", "true", Property.Type.BOOLEAN);
	private static Property lmbTweakWithItem = new Property("LMB tweak with item", "true", Property.Type.BOOLEAN);
	private static Property lmbTweakWithoutItem = new Property("LMB tweak without item", "true",
	                                                           Property.Type.BOOLEAN);
	private static Property wheelTweak = new Property("Wheel tweak", "true", Property.Type.BOOLEAN);
	private static Property wheelSearchOrder = new Property("Wheel tweak search order",
	                                                        "Last to first",
	                                                        Property.Type.STRING,
	                                                        new String[]{ "First to last", "Last to first" });
	private static Property wheelScrollDirection = new Property("Wheel tweak scroll direction",
	                                                            Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_NORMAL,
	                                                            Property.Type.STRING,
	                                                            new String[]{ Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_NORMAL,
	                                                                          Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_INVERTED,
	                                                                          Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_INVENTORY_POSITION_AWARE,
	                                                                          Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_INVENTORY_POSITION_AWARE_INVERTED });
	private static Property onTickMethodOrder = new Property("OnTick method order",
	                                                         "Forge, LiteLoader",
	                                                         Property.Type.STRING);
	private static Property scrollHandling = new Property("Scroll handling",
	                                                      "Smooth scrolling, minor issues",
	                                                      Property.Type.STRING,
	                                                      new String[]{ "Smooth scrolling, minor issues",
	                                                                    "Non-smooth scrolling, no issues" });
	private static Property scrollItemScaling = new Property("Scroll item scaling",
															 "Relative to scroll amount",
															 Property.Type.STRING,
															 new String[]{ "Relative to scroll amount",
																		   "Always exactly one item" });
	private static Property debug = new Property("Debug", "false", Property.Type.BOOLEAN);

	private boolean is_open = false;

	public ConfigGui(GuiScreen parentScreen) {
		super(parentScreen, getConfigElements(), Constants.MOD_ID, false, false, ".minecraft/config/MouseTweaks.cfg");

		rmbTweak.setComment(
			"Like vanilla right click dragging, but dragging over a slot multiple times puts the item there multiple "
			+ "times.");
		lmbTweakWithItem.setComment("Left click and drag with an item to \"left click\" items of the same type.");
		lmbTweakWithoutItem.setComment("Hold shift, left click and drag without an item to \"shift left click\" items.");
		wheelTweak.setComment("Scroll over items to move them between inventories.");
		wheelSearchOrder.setComment("How to pick the source slot when pulling items via scrolling.");
		wheelScrollDirection.setComment(
			"Inventory position aware means scroll up to push items from the bottom inventory and pull into the top "
			+ "inventory, and vice versa.");
		onTickMethodOrder.setComment(
			"This shouldn't really affect anything, but non-smooth scrolling works only with the Forge OnTick method.");
		scrollHandling.setComment(
			"When set to smooth scrolling, minor issues may be experienced such as scrolling \"through\" "
			+ "JEI or other mods. Non-smooth scrolling works only with the Forge OnTick method.");
		scrollItemScaling.setComment("This determines how many items are moved when you scroll.");
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
			wheelScrollDirection.set(scrollDirectionDescription());
			onTickMethodOrder.set(Main.config.onTickMethodOrderString());
			scrollHandling.set(scrollHandling.getValidValues()[Main.config.scrollHandling.ordinal()]);
			scrollItemScaling.set(scrollItemScaling.getValidValues()[Main.config.scrollItemScaling.ordinal()]);
			debug.set(Config.debug);
		}

		super.initGui();
	}

	private String scrollDirectionDescription() {
		WheelScrollDirection dir = Main.config.wheelScrollDirection;
		if (dir == WheelScrollDirection.NORMAL) {
			return Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_NORMAL;
		} else if (dir == WheelScrollDirection.INVERTED) {
			return Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_INVERTED;
		} else {
			return Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_INVENTORY_POSITION_AWARE;
		}
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
		Main.config.wheelScrollDirection = scrollDirectionFromDescription(wheelScrollDirection.getString());
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

	private WheelScrollDirection scrollDirectionFromDescription(String description) {
		switch (description) {
			case Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_NORMAL:
				return WheelScrollDirection.NORMAL;
			case Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_INVERTED:
				return WheelScrollDirection.INVERTED;
			case Constants.WHELL_SCROLL_DIRECTION_DESCRIPTION_INVENTORY_POSITION_AWARE:
				return WheelScrollDirection.INVENTORY_POSITION_AWARE;
			default:
				return WheelScrollDirection.INVENTORY_POSITION_AWARE_INVERTED;
		}
	}
}
