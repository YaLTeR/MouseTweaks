package yalter.mousetweaks;

public class ObfuscatedName {
	public final String mcpName;
	public final String forgeName;
	public final String vanillaName;

	ObfuscatedName(String mcpName, String forgeName, String vanillaName) {
		this.mcpName = mcpName;
		this.forgeName = forgeName;
		this.vanillaName = vanillaName;
	}

	String get(Obfuscation obf) {
		switch (obf) {
			case MCP:
				return mcpName;

			case FORGE:
				return forgeName;

			case VANILLA:
			default:
				return vanillaName;
		}
	}
}
