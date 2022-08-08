package yalter.mousetweaks;

public class Constants {
	public static final String MOD_NAME    = "Mouse Tweaks";
	public static final String MOD_DESC    = "Inventory mouse dragging and scrolling";
	public static final String MOD_ID      = "mod_MouseTweaks"; // Has to be this, otherwise Forge won't read mcmod.info.
	public static final String VERSION     = "2.7.2";
	public static final String UPDATE_URL  = "http://play.sourceruns.org/yalter/MouseTweaks/update.json";
	public static final String ICON        = "/mousetweaks_logo.png";
	
	static final String CONFIG_RMB_TWEAK              = "RMBTweak";
	static final String CONFIG_LMB_TWEAK_WITH_ITEM    = "LMBTweakWithItem";
	static final String CONFIG_LMB_TWEAK_WITHOUT_ITEM = "LMBTweakWithoutItem";
	static final String CONFIG_WHEEL_TWEAK            = "WheelTweak";
	static final String CONFIG_WHEEL_SEARCH_ORDER     = "WheelSearchOrder";
	static final String CONFIG_WHEEL_SCROLL_DIRECTION = "WheelScrollDirection";
	static final String CONFIG_ONTICK_METHOD_ORDER    = "OnTickMethodOrder";
	static final String CONFIG_DEBUG                  = "Debug";

	// Names for reflection.
	public static final ObfuscatedName GETSLOTATPOSITION_NAME = new ObfuscatedName("getSlotAtPosition", "a");
	public static final ObfuscatedName INVENTORY_FIELD_NAME = new ObfuscatedName("inventory", "e");

	static final String ONTICKMETHOD_FORGE_NAME      = "Forge";
	static final String ONTICKMETHOD_LITELOADER_NAME = "LiteLoader";

	static final int INVENTORY_SIZE = 36; // Size of the player inventory

	public enum EntryPoint {
		UNDEFINED,
		FORGE,
		LITELOADER
	}
}
