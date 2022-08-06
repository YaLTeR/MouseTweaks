package yalter.mousetweaks;

public class ObfuscatedName {
	public final String mcpName;
	public final String vanillaName;

	ObfuscatedName(String mcpName, String vanillaName) {
		this.mcpName = mcpName;
		this.vanillaName = vanillaName;
	}

	String get(Obfuscation obf) {
		switch (obf) {
			case MCP:
				return mcpName;

			case VANILLA:
			default:
				return vanillaName;
		}
	}
}
