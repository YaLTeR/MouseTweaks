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
	protected GuiContainerCreative guiContainerCreative;

	public GuiContainerCreativeHandler(GuiContainerCreative guiContainerCreative) {
		super(guiContainerCreative);
		this.guiContainerCreative = guiContainerCreative;
	}

	@Override
	public boolean isMouseTweaksDisabled() {
		return (super.isMouseTweaksDisabled() || Reflection.guiContainerCreative == null);
	}

	@Override
	public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
		try {
			Reflection.guiContainerCreative.invokeMethod(guiContainerCreative,
			                                             Constants.HANDLEMOUSECLICK_NAME.forgeName,
			                                             slot,
			                                             slot.slotNumber,
			                                             mouseButton.getValue(),
			                                             shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP);
		} catch (InvocationTargetException e) {
			CrashReport crashreport = CrashReport.makeCrashReport(e, "GuiContainerCreative.handleMouseClick() threw an exception when called from MouseTweaks");
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public boolean isIgnored(Slot slot) {
		return (super.isIgnored(slot) || slot.inventory != mc.player.inventory);
	}
}
