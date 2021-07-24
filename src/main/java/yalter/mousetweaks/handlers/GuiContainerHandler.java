package yalter.mousetweaks.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.world.inventory.*;
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
	private AbstractContainerScreen guiContainer;
	private Method handleMouseClick;

	public GuiContainerHandler(AbstractContainerScreen guiContainer) {
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
		return guiContainer.getMenu().slots;
	}

	@Override
	public Slot getSlotUnderMouse(double mouseX, double mouseY) {
		try {
			return (Slot) Reflection.guiContainerClass.invokeMethod(guiContainer,
			                                                        Constants.GETSELECTEDSLOT_NAME.forgeName,
			                                                        mouseX,
			                                                        mouseY);
		} catch (InvocationTargetException e) {
			CrashReport crashreport = CrashReport.forThrowable(e,
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
			                        slot.index,
			                        mouseButton.getValue(),
			                        shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP);
		} catch (InvocationTargetException e) {
			CrashReport crashreport = CrashReport.forThrowable(e,
					                                  "handleMouseClick() threw an exception when called "
							                                   + "from MouseTweaks.");
			throw new ReportedException(crashreport);
		} catch (IllegalAccessException e) {
			CrashReport crashreport = CrashReport.forThrowable(e, "Calling handleMouseClick() from MouseTweaks.");
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public boolean isCraftingOutput(Slot slot) {
		return (slot instanceof ResultSlot
		        || slot instanceof FurnaceResultSlot
		        || slot instanceof MerchantResultSlot);
	}

	@Override
	public boolean isIgnored(Slot slot) {
		return false;
	}
}
