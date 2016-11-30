package yalter.mousetweaks.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.crash.CrashReport;
import net.minecraft.inventory.*;
import net.minecraft.util.ReportedException;
import org.lwjgl.input.Mouse;
import yalter.mousetweaks.Constants;
import yalter.mousetweaks.IGuiScreenHandler;
import yalter.mousetweaks.MouseButton;
import yalter.mousetweaks.Reflection;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

public class GuiContainerHandler implements IGuiScreenHandler {
	private Minecraft mc;
	private GuiContainer guiContainer;

	public GuiContainerHandler(GuiContainer guiContainer) {
		this.mc = Minecraft.getMinecraft();
		this.guiContainer = guiContainer;
	}

	private int getDisplayWidth() {
		return mc.displayWidth;
	}

	private int getDisplayHeight() {
		return mc.displayHeight;
	}

	private int getRequiredMouseX() {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		return (Mouse.getX() * scaledResolution.getScaledWidth()) / getDisplayWidth();
	}

	private int getRequiredMouseY() {
		ScaledResolution scaledResolution = new ScaledResolution(mc);
		int scaledHeight = scaledResolution.getScaledHeight();
		return scaledHeight - ((Mouse.getY() * scaledHeight) / getDisplayHeight()) - 1;
	}

	@Override
	public boolean isMouseTweaksDisabled() {
		return (Reflection.guiContainerClass == null);
	}

	@Override
	public boolean isWheelTweakDisabled() {
		return false;
	}

	@Override
	public List<Slot> getSlots() {
		return guiContainer.inventorySlots.inventorySlots;
	}

	@Override
	public Slot getSlotUnderMouse() {
		try {
			return (Slot)Reflection.guiContainerClass.invokeMethod(guiContainer, Constants.GETSLOTATPOSITION_NAME.forgeName, getRequiredMouseX(), getRequiredMouseY());
		} catch (InvocationTargetException e) {
			CrashReport crashreport = CrashReport.makeCrashReport(e, "GuiContainer.getSlotAtPosition() threw an exception when called from MouseTweaks");
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public boolean disableRMBDraggingFunctionality() {
		if ((Boolean)Reflection.guiContainerClass.getFieldValue(guiContainer, Constants.DRAGSPLITTING_NAME.forgeName)) {
			if ((Integer)Reflection.guiContainerClass.getFieldValue(guiContainer, Constants.DRAGSPLITTINGBUTTON_NAME.forgeName) == 1) {
				Reflection.guiContainerClass.setFieldValue(guiContainer, Constants.DRAGSPLITTING_NAME.forgeName, false);
				Reflection.guiContainerClass.setFieldValue(guiContainer, Constants.IGNOREMOUSEUP_NAME.forgeName, true);

				Set<Slot> slots = (Set<Slot>)Reflection.guiContainerClass.getFieldValue(guiContainer, Constants.DRAGSPLITTINGSLOTS_NAME.forgeName);
				for (Slot slot : slots)
					clickSlot(slot, MouseButton.RIGHT, false);

				return slots.isEmpty();
			}
		}

		return false;
	}

	@Override
	public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
		mc.playerController.windowClick(guiContainer.inventorySlots.windowId,
		                                slot.slotNumber,
		                                mouseButton.getValue(),
		                                shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP,
		                                mc.player);
	}

	@Override
	public boolean isCraftingOutput(Slot slot) {
		return (slot instanceof SlotCrafting
			|| slot instanceof SlotFurnaceOutput
			|| slot instanceof SlotMerchantResult
			|| (guiContainer.inventorySlots instanceof ContainerRepair && slot.slotNumber == 2));
	}

	@Override
	public boolean isIgnored(Slot slot) {
		return false;
	}
}
