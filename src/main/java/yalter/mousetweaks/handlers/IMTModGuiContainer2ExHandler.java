package yalter.mousetweaks.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import yalter.mousetweaks.IGuiScreenHandler;
import yalter.mousetweaks.MouseButton;
import yalter.mousetweaks.api.IMTModGuiContainer2Ex;

import java.util.List;

public class IMTModGuiContainer2ExHandler implements IGuiScreenHandler {
	protected Minecraft mc;
	protected IMTModGuiContainer2Ex modGuiContainer;

	public IMTModGuiContainer2ExHandler(IMTModGuiContainer2Ex modGuiContainer) {
		this.mc = Minecraft.getMinecraft();
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
	public Slot getSlotUnderMouse() {
		return modGuiContainer.MT_getSlotUnderMouse();
	}

	@Override
	public boolean disableRMBDraggingFunctionality() {
		return modGuiContainer.MT_disableRMBDraggingFunctionality();
	}

	@Override
	public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
		modGuiContainer.MT_clickSlot(slot, mouseButton.getValue(), shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP);
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
