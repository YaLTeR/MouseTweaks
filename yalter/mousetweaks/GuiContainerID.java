package yalter.mousetweaks;

public enum GuiContainerID {
	NOTASSIGNED,        // Not determined yet.
	NOTGUICONTAINER,    // This is not a container GUI.
	MINECRAFT,          // Containers that are compatible with vanilla Minecraft ones.
	MTMODGUICONTAINER   // Containers that implement the IMTModGuiContainer interface.
}
