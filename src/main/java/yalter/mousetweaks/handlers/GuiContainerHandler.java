package yalter.mousetweaks.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.inventory.container.*;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.IGuiScreenHandler;
import yalter.mousetweaks.MouseButton;
import yalter.mousetweaks.Reflection;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;
import yalter.mousetweaks.api.MouseTweaksIgnore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class GuiContainerHandler implements IGuiScreenHandler {
	Minecraft mc;
	private ContainerScreen guiContainer;
	private Method handleMouseClick;

	public GuiContainerHandler(ContainerScreen guiContainer) {
		this.mc = Minecraft.getInstance();
		this.guiContainer = guiContainer;
		this.handleMouseClick = Reflection.getHMCMethod(guiContainer);
	}

	@Override
	public boolean isMouseTweaksDisabled() {
		return guiContainer.getClass().isAnnotationPresent(MouseTweaksIgnore.class) || (Reflection.guiContainerClass
		                                                                                == null);
	}

	@Override
	public boolean isWheelTweakDisabled() {
		return guiContainer.getClass().isAnnotationPresent(MouseTweaksDisableWheelTweak.class);
	}

	@Override
	public List<Slot> getSlots() {
		return guiContainer.getContainer().inventorySlots;
	}

	@Override
	public Slot getSlotUnderMouse(double mouseX, double mouseY) {
		try {
			return (Slot) Reflection.guiContainerClass.invokeMethod(guiContainer,
			                                                        Constants.GETSELECTEDSLOT_NAME.forgeName,
			                                                        mouseX,
			                                                        mouseY);
		} catch (InvocationTargetException e) {
			CrashReport crashreport = CrashReport.makeCrashReport(e,
			                                                      "GuiContainer.getSlotAtPosition() threw an exception"
			                                                      + " when called from MouseTweaks.");
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public boolean disableRMBDraggingFunctionality() {
		Reflection.guiContainerClass.setFieldValue(guiContainer, Constants.IGNOREMOUSEUP_NAME.forgeName, true);

		if ((Boolean) Reflection.guiContainerClass.getFieldValue(guiContainer,
		                                                         Constants.DRAGSPLITTING_NAME.forgeName)) {
			if ((Integer) Reflection.guiContainerClass.getFieldValue(guiContainer,
			                                                         Constants.DRAGSPLITTINGBUTTON_NAME.forgeName)
			    == 1) {
				Reflection.guiContainerClass.setFieldValue(guiContainer, Constants.DRAGSPLITTING_NAME.forgeName,
				                                           false);
				return true;
			}
		}

		return false;
	}

	@Override
	public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
		try {
			handleMouseClick.invoke(guiContainer,
			                        slot,
			                        slot.slotNumber,
			                        mouseButton.getValue(),
			                        shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP);
		} catch (InvocationTargetException e) {
			CrashReport crashreport = CrashReport.makeCrashReport(e,
			                                                      "handleMouseClick() threw an exception when called "
			                                                      + "from MouseTweaks.");
			throw new ReportedException(crashreport);
		} catch (IllegalAccessException e) {
			CrashReport crashreport = CrashReport.makeCrashReport(e, "Calling handleMouseClick() from MouseTweaks.");
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public boolean isCraftingOutput(Slot slot) {
		return (slot instanceof CraftingResultSlot
		        || slot instanceof FurnaceResultSlot
		        || slot instanceof MerchantResultSlot
		        || (guiContainer.getContainer() instanceof RepairContainer && slot.slotNumber == 2));
	}

	@Override
	public boolean isIgnored(Slot slot) {
		return false;
	}
}
