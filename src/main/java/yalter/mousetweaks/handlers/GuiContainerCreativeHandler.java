package yalter.mousetweaks.handlers;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.Slot;

public class GuiContainerCreativeHandler extends GuiContainerHandler {
	public GuiContainerCreativeHandler(GuiContainerCreative guiContainerCreative) {
		super(guiContainerCreative);
	}

	@Override
	public boolean isIgnored(Slot slot) {
		return (super.isIgnored(slot) || slot.inventory != mc.player.inventory);
	}
}
