package yalter.mousetweaks;

public class Constants {
	public static final String MOD_NAME = "Mouse Tweaks";
	public static final String MOD_ID = "mousetweaks";
	public static final String VERSION = "@VERSION@";
	public static final String UPDATE_URL = "http://play.sourceruns.org/yalter/MouseTweaks/update.json";

	static final String CONFIG_RMB_TWEAK = "RMBTweak";
	static final String CONFIG_LMB_TWEAK_WITH_ITEM = "LMBTweakWithItem";
	static final String CONFIG_LMB_TWEAK_WITHOUT_ITEM = "LMBTweakWithoutItem";
	static final String CONFIG_WHEEL_TWEAK = "WheelTweak";
	static final String CONFIG_WHEEL_SEARCH_ORDER = "WheelSearchOrder";
	static final String CONFIG_WHEEL_SCROLL_DIRECTION = "WheelScrollDirection";
	static final String CONFIG_ONTICK_METHOD_ORDER = "OnTickMethodOrder";
	static final String CONFIG_SCROLL_HANDLING = "ScrollHandling";
	static final String CONFIG_DEBUG = "Debug";
	static final String CONFIG_SCROLL_ITEM_SCALING = "ScrollItemScaling";

	// Names for reflection.
	public static final ObfuscatedName IGNOREMOUSEUP_NAME = new ObfuscatedName("ignoreMouseUp", "field_146995_H", "I");
	public static final ObfuscatedName DRAGSPLITTING_NAME = new ObfuscatedName("dragSplitting", "field_147007_t", "u");
	public static final ObfuscatedName DRAGSPLITTINGBUTTON_NAME = new ObfuscatedName("dragSplittingButton",
	                                                                                 "field_146988_G",
	                                                                                 "H");
	public static final ObfuscatedName GETSLOTATPOSITION_NAME = new ObfuscatedName("getSlotAtPosition",
	                                                                               "func_146975_c",
	                                                                               "d");
	public static final ObfuscatedName HANDLEMOUSECLICK_NAME = new ObfuscatedName("handleMouseClick",
	                                                                              "func_184098_a",
	                                                                              "a");

	static final String ONTICKMETHOD_FORGE_NAME = "Forge";
	static final String ONTICKMETHOD_LITELOADER_NAME = "LiteLoader";

	public static final String WHELL_SCROLL_DIRECTION_DESCRIPTION_NORMAL = "Down to push, up to pull";
	public static final String WHELL_SCROLL_DIRECTION_DESCRIPTION_INVERTED = "Up to push, down to pull";
	public static final String WHELL_SCROLL_DIRECTION_DESCRIPTION_INVENTORY_POSITION_AWARE = "Inventory position aware";
	public static final String WHELL_SCROLL_DIRECTION_DESCRIPTION_INVENTORY_POSITION_AWARE_INVERTED = "Inventory position aware, inverted";

	static final int INVENTORY_SIZE = 36; // Size of the player inventory

	public enum EntryPoint {
		UNDEFINED, FORGE, LITELOADER
	}
}
