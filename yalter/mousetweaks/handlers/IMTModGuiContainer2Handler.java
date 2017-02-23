package yalter.mousetweaks.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Slot;
import net.minecraft.src.ModLoader;
import yalter.mousetweaks.IGuiScreenHandler;
import yalter.mousetweaks.MouseButton;
import yalter.mousetweaks.api.IMTModGuiContainer2;

import java.util.List;

public class IMTModGuiContainer2Handler implements IGuiScreenHandler {
	protected Minecraft mc;
	protected IMTModGuiContainer2 modGuiContainer;

	public IMTModGuiContainer2Handler(IMTModGuiContainer2 modGuiContainer) {
		this.mc = ModLoader.getMinecraftInstance();
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
		return modGuiContainer.MT_getContainer().inventorySlots;
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
		mc.playerController.windowClick(modGuiContainer.MT_getContainer().windowId,
		                                slot.slotNumber,
		                                mouseButton.getValue(),
		                                shiftPressed,
		                                mc.thePlayer);
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
