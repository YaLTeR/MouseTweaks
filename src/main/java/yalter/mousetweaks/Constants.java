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
			= new ObfuscatedName("skipNextRelease", "f_97719_", "Q");
	public static final ObfuscatedName DRAGSPLITTING_NAME
			= new ObfuscatedName("isQuickCrafting", "f_97738_", "y");
	public static final ObfuscatedName DRAGSPLITTINGBUTTON_NAME
			= new ObfuscatedName("quickCraftingButton", "f_97718_", "P");
	public static final ObfuscatedName GETSELECTEDSLOT_NAME
			= new ObfuscatedName("findSlot", "m_97744_", "a");
	static final ObfuscatedName HANDLEMOUSECLICK_NAME
			= new ObfuscatedName("slotClicked", "m_6597_", "a");
}
