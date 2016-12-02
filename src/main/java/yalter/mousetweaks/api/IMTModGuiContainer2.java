package yalter.mousetweaks.api;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/**
 * This is the interface you want to implement in your GuiScreen to make it compatible with Mouse Tweaks.
 */
public interface IMTModGuiContainer2 {
	/**
	 * If you want to disable Mouse Tweaks in your GuiScreen, return true from this method.
	 *
	 * @return True if Mouse Tweaks should be disabled, false otherwise.
	 */
	boolean isMouseTweaksDisabled();

	/**
	 * If you want to disable the Wheel Tweak in your GuiScreen, return true from this method.
	 *
	 * @return True if the Wheel Tweak should be disabled, false otherwise.
	 */
	boolean isWheelTweakDisabled();

	/**
	 * Returns the Container.
	 *
	 * @return Container that is currently in use.
	 */
	Container getContainer();

	/**
	 * Returns the Slot that is currently selected by the player, or null if no Slot is selected.
	 *
	 * @return Slot that is located under the mouse, or null if no Slot it currently under the mouse.
	 */
	Slot getSlotUnderMouse();

	/**
	 * Return true if the given Slot behaves like the vanilla crafting output slots (inside the crafting table,
	 * or the furnace output slot, or the anvil output slot, etc.). These slots are handled differently by Mouse Tweaks.
	 *
	 * @param slot the slot to check
	 * @return True if slot is a crafting output slot.
	 */
	boolean isCraftingOutput(Slot slot);

	/**
	 * Return true if the given Slot should be ignored by Mouse Tweaks. Examples of ignored slots are the item select
	 * slots and the Destroy Item slot in the vanilla creative inventory.
	 *
	 * @param slot the slot to check
	 * @return Tru if slot should be ignored by Mouse Tweaks.
	 */
	boolean isIgnored(Slot slot);

	/**
	 * If your container has an RMB dragging functionality (like vanilla containers), disable it inside this method.
	 * This method is called every frame (render tick), which is after all mouseClicked / mouseClickMove / mouseReleased
	 * events are handled (although note these events are handled every game tick, which is far less frequent than every
	 * render tick).
	 *
	 * If true is returned from this method, Mouse Tweaks will click the slot on which the right mouse
	 * button was initially pressed (in most cases this is the slot currently under mouse). This is needed because
	 * the vanilla RMB dragging functionality prevents the initial slot click.
	 *
	 * For vanilla containers this method looks like this:
	 * <pre>
	 * this.ignoreMouseUp = true;
	 *
	 * if (this.dragSplitting) {
	 *     if (this.dragSplittingButton == 1) {
	 *         this.dragSplitting = false;
	 *         return true;
	 *     }
	 * }
	 *
	 * return false;
	 * </pre>
	 *
	 * @return True if Mouse Tweaks should click the slot on which the RMB was pressed.
	 */
	boolean disableRMBDraggingFunctionality();
}
