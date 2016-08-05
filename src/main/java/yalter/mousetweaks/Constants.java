package yalter.mousetweaks;

public class Constants {
	public static final String MOD_NAME    = "Mouse Tweaks";
	public static final String MOD_ID      = "mousetweaks";
	public static final String VERSION     = "@VERSION@";
	public static final String UPDATE_URL  = "http://play.sourceruns.org/yalter/MouseTweaks/update.json";

	public static final String CONFIG_RMB_TWEAK              = "RMBTweak";
	public static final String CONFIG_LMB_TWEAK_WITH_ITEM    = "LMBTweakWithItem";
	public static final String CONFIG_LMB_TWEAK_WITHOUT_ITEM = "LMBTweakWithoutItem";
	public static final String CONFIG_WHEEL_TWEAK            = "WheelTweak";
	public static final String CONFIG_WHEEL_SEARCH_ORDER     = "WheelSearchOrder";
	public static final String CONFIG_WHEEL_SCROLL_DIRECTION = "WheelScrollDirection";
	public static final String CONFIG_ONTICK_METHOD_ORDER    = "OnTickMethodOrder";
	public static final String CONFIG_DEBUG                  = "Debug";

	// Obfuscated names
	public static final String ISMOUSEOVERSLOT_NAME = "a";
	public static final String FIELDE_NAME          = "H";
	public static final String FIELDq_NAME          = "t";

	// Forge obfuscated names
	public static final String ISMOUSEOVERSLOT_FORGE_NAME = "func_146981_a";
	public static final String FIELDE_FORGE_NAME          = "field_146995_H";
	public static final String FIELDq_FORGE_NAME          = "field_147007_t";

	// MCP names
	public static final String ISMOUSEOVERSLOT_MCP_NAME = "isMouseOverSlot";
	public static final String FIELDE_MCP_NAME          = "ignoreMouseUp";
	public static final String FIELDq_MCP_NAME          = "dragSplitting";

	public static final String ONTICKMETHOD_FORGE_NAME      = "Forge";
	public static final String ONTICKMETHOD_LITELOADER_NAME = "LiteLoader";

	public static final int INVENTORY_SIZE = 36; // Size of the player inventory

	public enum EntryPoint {
		UNDEFINED,
		FORGE,
		LITELOADER
	}
}
