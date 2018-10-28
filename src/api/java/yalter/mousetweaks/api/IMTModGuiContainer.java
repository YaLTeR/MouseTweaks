package yalter.mousetweaks.api;

/**
 * This is an old version of the API interface and it's very inconvenient to work with.
 * Check IMTModGuiContainer2 instead.
 */
@Deprecated
public interface IMTModGuiContainer {
	/**
	 * Returns the API version.
	 *
	 * @return The API version. Should return 1 for this version.
	 */
	int getAPIVersion();

	/**
	 * Returns the mod name to use in log messages.
	 *
	 * @return The name of the mod.
	 */
	String getModName();

	/**
	 * Returns whether or not Mouse Tweaks should be disabled for this GuiContainer.
	 *
	 * @return True if Mouse Tweaks should be disabled, false otherwise.
	 */
	boolean isMouseTweaksDisabled();

	/**
	 * Returns whether or not wheelTweak should be disabled for this GuiContainer.
	 *
	 * @return True if wheelTweak should be disabled, false otherwise.
	 */
	boolean isWheelTweakDisabled();

	/**
	 * Returns whether or not the given slot is a crafting output slot.
	 *
	 * @param modContainer The container of the given slot.
	 * @param slot         The slot to check.
	 * @return True if the given slot is a crafting output slot, false otherwise.
	 */
	boolean isCraftingOutputSlot(Object modContainer, Object slot);

	/**
	 * Returns the container of this GuiContainer. If multiple containers are used, returns the one that is used at
	 * the moment of the method execution.
	 *
	 * @return The container that is currently used.
	 */
	Object getModContainer();

	/**
	 * Returns the number of slots present in the given container (for containers with variable slot number - the
	 * number of slots at the moment of the method execution).
	 *
	 * @param modContainer The container to retrieve the number of slots from.
	 * @return The number of slots in the given container.
	 */
	int getModSlotCount(Object modContainer);

	/**
	 * Returns a slot from this container with the given slot number.
	 *
	 * @param modContainer The container from which to retrieve the slot.
	 * @param slotNumber   The number of the slot to be retrieved.
	 * @return The slot from the given container with the given slot number.
	 */
	Object getModSlot(Object modContainer, int slotNumber);

	/**
	 * Returns the currently selected slot (the one under the player's mouse cursor) of the given container, which has
	 * the given number of slots.
	 *
	 * @param modContainer The container to check.
	 * @param slotCount    The number of slots that this container has.
	 * @return The slot that is currently selected. Null if none are.
	 */
	Object getModSelectedSlot(Object modContainer, int slotCount);

	/**
	 * The method that clicks the given slot of the given container with the given mouse button and shift key state.
	 *
	 * @param modContainer The container of the given slot.
	 * @param slot         The slot to click.
	 * @param mouseButton  The mouse button. 0 is the left one, 1 is the right one.
	 * @param shiftPressed True if the shift key is pressed, false otherwise.
	 */
	void clickModSlot(Object modContainer, Object slot, int mouseButton, boolean shiftPressed);

	/**
	 * The method that disables the built-in RMB drag mechanic, if required, in this GuiContainer, with the given
	 * first slot in the given container to click if shouldClick is set to true (in vanilla circumstances - your RMB
	 * dragging mechanic might work differently).
	 *
	 * @param modContainer The container of the first slot.
	 * @param firstSlot    The first slot.
	 * @param shouldClick  True, if the first slot is to be clicked.
	 */
	void disableRMBDragIfRequired(Object modContainer, Object firstSlot, boolean shouldClick);
}