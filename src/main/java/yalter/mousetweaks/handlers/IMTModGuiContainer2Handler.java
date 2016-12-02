package yalter.mousetweaks.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import yalter.mousetweaks.IGuiScreenHandler;
import yalter.mousetweaks.MouseButton;
import yalter.mousetweaks.api.IMTModGuiContainer2;

import java.util.List;

public class IMTModGuiContainer2Handler implements IGuiScreenHandler {
	protected Minecraft mc;
	protected IMTModGuiContainer2 modGuiContainer;

	public IMTModGuiContainer2Handler(IMTModGuiContainer2 modGuiContainer) {
		this.mc = Minecraft.getMinecraft();
		this.modGuiContainer = modGuiContainer;
	}

	@Override
	public boolean isMouseTweaksDisabled() {
		return modGuiContainer.isMouseTweaksDisabled();
	}

	@Override
	public boolean isWheelTweakDisabled() {
		return modGuiContainer.isWheelTweakDisabled();
	}

	@Override
	public List<Slot> getSlots() {
		return modGuiContainer.getContainer().inventorySlots;
	}

	@Override
	public Slot getSlotUnderMouse() {
		return modGuiContainer.getSlotUnderMouse();
	}

	@Override
	public boolean disableRMBDraggingFunctionality() {
		return modGuiContainer.disableRMBDraggingFunctionality();
	}

	@Override
	public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
		mc.playerController.windowClick(modGuiContainer.getContainer().windowId,
		                                slot.slotNumber,
		                                mouseButton.getValue(),
		                                shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP,
		                                mc.player);
	}

	@Override
	public boolean isCraftingOutput(Slot slot) {
		return modGuiContainer.isCraftingOutput(slot);
	}

	@Override
	public boolean isIgnored(Slot slot) {
		return modGuiContainer.isIgnored(slot);
	}
}
