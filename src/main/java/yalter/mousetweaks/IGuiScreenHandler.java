package yalter.mousetweaks;

import net.minecraft.inventory.Slot;

import java.util.List;

public interface IGuiScreenHandler {
	boolean isMouseTweaksDisabled();

	boolean isWheelTweakDisabled();

	List<Slot> getSlots();

	Slot getSlotUnderMouse();

	boolean disableRMBDraggingFunctionality();

	void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed);

	boolean isCraftingOutput(Slot slot);

	boolean isIgnored(Slot slot);
}
