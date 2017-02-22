package yalter.mousetweaks.handlers;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.crash.CrashReport;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ReportedException;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.MouseButton;
import yalter.mousetweaks.Reflection;

import java.lang.reflect.InvocationTargetException;

public class GuiContainerCreativeHandler extends GuiContainerHandler {
	public GuiContainerCreativeHandler(GuiContainerCreative guiContainerCreative) {
		super(guiContainerCreative);
	}

	@Override
	public boolean isIgnored(Slot slot) {
		return (super.isIgnored(slot) || slot.inventory != mc.player.inventory);
	}
}
