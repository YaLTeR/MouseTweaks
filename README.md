# Mouse Tweaks
A mod that enhances the inventory management by adding various additional functions to the usual mouse buttons.

# How to build
- Set up MCP v4.2 or v4.3 for Minecraft 1.7.2 or 1.7.3 with ModLoader.
- Copy stuff from this repository into `src/minecraft/`.
- Recompile / reobfuscate.

# Compatibility
Mouse Tweaks assumes a GuiScreen has some object (Container) which can provide number of slots, slots by indices and the currently selected slot. By default Mouse Tweaks works with all standard GuiContainer-s. If your inventory doesn't extend the standard GuiContainer, you can implement the Mouse Tweaks API interface (`yalter.mousetweaks.api.IMTModGuiContainer`) to provide the necessary functions.
