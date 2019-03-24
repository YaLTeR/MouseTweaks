package yalter.mousetweaks.handlers;

import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import yalter.mousetweaks.IGuiScreenHandler;
import yalter.mousetweaks.MouseButton;
import yalter.mousetweaks.api.IMTModGuiContainer3Ex;

import java.util.List;

public class IMTModGuiContainer3ExHandler implements IGuiScreenHandler {
	private IMTModGuiContainer3Ex modGuiContainer;

	public IMTModGuiContainer3ExHandler(IMTModGuiContainer3Ex modGuiContainer) {
		this.modGuiContainer = modGuiContainer;
	}

	@Override
	public boolean isMouseTweaksDisabled() {
		return modGuiContainer.MT_isMouseTweaksDisabled();
	}

	@Override
	public boolean isWheelTweakDisabled() {
		return modGuiContainer.MT_isWheelTweakDisabled();
	}

	@Override
	public List<Slot> getSlots() {
		return modGuiContainer.MT_getSlots();
	}

	@Override
	public Slot getSlotUnderMouse(double mouseX, double mouseY) {
		return modGuiContainer.MT_getSlotUnderMouse(mouseX, mouseY);
	}

	@Override
	public boolean disableRMBDraggingFunctionality() {
		return modGuiContainer.MT_disableRMBDraggingFunctionality();
	}

	@Override
	public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
		modGuiContainer.MT_clickSlot(slot,
		                             mouseButton.getValue(),
		                             shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP);
	}

	@Override
	public boolean isCraftingOutput(Slot slot) {
		return modGuiContainer.MT_isCraftingOutput(slot);
	}

	@Override
	public boolean isIgnored(Slot slot) {
		return modGuiContainer.MT_isIgnored(slot);
	}
}
