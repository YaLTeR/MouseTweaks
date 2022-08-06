package yalter.mousetweaks.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ScaledResolution;
import net.minecraft.src.Slot;
import net.minecraft.src.SlotCrafting;
import net.minecraft.src.SlotFurnace;

import org.lwjgl.input.Mouse;

import yalter.mousetweaks.Constants;
import yalter.mousetweaks.IGuiScreenHandler;
import yalter.mousetweaks.MouseButton;
import yalter.mousetweaks.Reflection;

public class GuiContainerHandler implements IGuiScreenHandler {
	protected Minecraft mc;
	protected GuiContainer guiContainer;

	public GuiContainerHandler(GuiContainer guiContainer) {
		this.mc = ModLoader.getMinecraftInstance();
		this.guiContainer = guiContainer;
	}

	private int getDisplayWidth() {
		return mc.displayWidth;
	}

	private int getDisplayHeight() {
		return mc.displayHeight;
	}

	private int getRequiredMouseX() {
		ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, getDisplayWidth(), getDisplayHeight());
		return (Mouse.getX() * scaledResolution.getScaledWidth()) / getDisplayWidth();
	}

	private int getRequiredMouseY() {
		ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, getDisplayWidth(), getDisplayHeight());
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
		return guiContainer.inventorySlots.slots;
	}

	@Override
	public Slot getSlotUnderMouse() {
		try {
			return (Slot)Reflection.guiContainerClass.invokeMethod(guiContainer, Constants.GETSLOTATPOSITION_NAME.mcpName, getRequiredMouseX(), getRequiredMouseY());
		} catch (InvocationTargetException e) {
			ModLoader.ThrowException("GuiContainer.getSlotAtPosition() threw an exception when called from MouseTweaks.", e);
			return null;
		}
	}

	@Override
	public boolean disableRMBDraggingFunctionality() {
		return false;
	}

	@Override
	public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
		mc.playerController.func_27174_a(guiContainer.inventorySlots.windowId,
                    slot.slotNumber,
                    mouseButton.getValue(),
                    shiftPressed,
                    mc.thePlayer);
	}

	@Override
	public boolean isCraftingOutput(Slot slot) {
		return (slot instanceof SlotCrafting
			|| slot instanceof SlotFurnace);
	}

	@Override
	public boolean isIgnored(Slot slot) {
		return false;
	}
}
