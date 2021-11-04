package yalter.mousetweaks;

public class ObfuscatedName {
	public final String mcpName;
	public final String forgeName;
	public final String loomName;
	public final String vanillaName;

	ObfuscatedName(String mcpName, String forgeName, String loomName, String vanillaName) {
		this.mcpName = mcpName;
		this.forgeName = forgeName;
		this.loomName = loomName;
		this.vanillaName = vanillaName;
	}

	String get(Obfuscation obf) {
		switch (obf) {
			case MCP:
				return mcpName;

			case FORGE:
				return forgeName;

			case LOOM:
				return loomName;

			case VANILLA:
			default:
				return vanillaName;
		}
	}
}
