package yalter.mousetweaks;

public class Constants {
	public static final String MOD_NAME    = "Mouse Tweaks";
	public static final String MOD_ID      = "MouseTweaks";
	public static final String VERSION     = "@VERSION@";
	public static final String UPDATE_URL  = "http://play.sourceruns.org/yalter/MouseTweaks/update.json";

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

	// OnTick method names
	public static final String ONTICKMETHOD_FORGE_NAME      = "Forge";
	public static final String ONTICKMETHOD_LITELOADER_NAME = "LiteLoader";

	// Inventory-related stuff
	public static final int INVENTORY_SIZE = 36; // Size of the player inventory

	public enum EntryPoint {
		UNDEFINED,
		FORGE,
		LITELOADER
	}
}
