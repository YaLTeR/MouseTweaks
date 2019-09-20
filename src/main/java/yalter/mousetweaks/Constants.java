package yalter.mousetweaks;

public class Constants {
	public static final String MOD_ID = "mousetweaks";

	static final String CONFIG_RMB_TWEAK = "RMBTweak";
	static final String CONFIG_LMB_TWEAK_WITH_ITEM = "LMBTweakWithItem";
	static final String CONFIG_LMB_TWEAK_WITHOUT_ITEM = "LMBTweakWithoutItem";
	static final String CONFIG_WHEEL_TWEAK = "WheelTweak";
	static final String CONFIG_WHEEL_SEARCH_ORDER = "WheelSearchOrder";
	static final String CONFIG_WHEEL_SCROLL_DIRECTION = "WheelScrollDirection";
	static final String CONFIG_DEBUG = "Debug";
	static final String CONFIG_SCROLL_ITEM_SCALING = "ScrollItemScaling";

	// Names for reflection.
	public static final ObfuscatedName IGNOREMOUSEUP_NAME
			= new ObfuscatedName("ignoreMouseUp", "field_146995_H", "w");
	public static final ObfuscatedName DRAGSPLITTING_NAME
			= new ObfuscatedName("dragSplitting", "field_147007_t", "j");
	public static final ObfuscatedName DRAGSPLITTINGBUTTON_NAME
			= new ObfuscatedName("dragSplittingButton", "field_146988_G", "v");
	public static final ObfuscatedName GETSELECTEDSLOT_NAME
			= new ObfuscatedName("getSelectedSlot", "func_195360_a", "c");
	static final ObfuscatedName HANDLEMOUSECLICK_NAME
			= new ObfuscatedName("handleMouseClick", "func_184098_a", "a");
}
