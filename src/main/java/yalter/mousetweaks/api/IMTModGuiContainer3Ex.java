package yalter.mousetweaks.api;

import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * This is the interface you want to implement in your custom container screen to make it compatible with Mouse Tweaks.
 */
public interface IMTModGuiContainer3Ex {
    /**
     * If you want to disable Mouse Tweaks in your GuiScreen, return true from this method.
     *
     * @return True if Mouse Tweaks should be disabled, false otherwise.
     */
    boolean MT_isMouseTweaksDisabled();

    /**
     * If you want to disable the Wheel Tweak in your AbstractContainerScreen, return true from this method.
     *
     * @return True if the Wheel Tweak should be disabled, false otherwise.
     */
    boolean MT_isWheelTweakDisabled();

    /**
     * Returns a list of Slots currently present in the inventory.
     * For vanilla containers it is this.getMenu().slots.
     *
     * @return List of Slots currently present in the inventory.
     */
    List<Slot> MT_getSlots();

    /**
     * Returns the Slot located under the given mouse coordinates, or null if no Slot is selected. For vanilla
     * containers it is this.findSlot().
     *
     * @param mouseX Mouse X.
     * @param mouseY Mouse Y.
     * @return Slot that is located under the mouse, or null if no Slot it currently under the mouse.
     */
    Slot MT_getSlotUnderMouse(double mouseX, double mouseY);

    /**
     * Return true if the given Slot behaves like the vanilla crafting output slots (inside the crafting table,
     * or the furnace output slot, or the anvil output slot, etc.). These slots are handled differently by Mouse Tweaks.
     *
     * @param slot the slot to check
     * @return True if slot is a crafting output slot.
     */
    boolean MT_isCraftingOutput(Slot slot);

    /**
     * Return true if the given Slot should be ignored by Mouse Tweaks. Examples of ignored slots are the item select
     * slots and the Destroy Item slot in the vanilla creative inventory.
     *
     * @param slot the slot to check
     * @return Tru if slot should be ignored by Mouse Tweaks.
     */
    boolean MT_isIgnored(Slot slot);

    /**
     * Disables the vanilla quick crafting functionality and sets the next mouse release to be ignored.<br><br>
     * <p>
     * For vanilla containers this method looks like this:
     * <pre>
     * this.skipNextRelease = true;
     *
     * if (this.isQuickCrafting && this.quickCraftingButton == 1) {
     *     this.isQuickCrafting = false;
     *     return true;
     * }
     *
     * return false;
     * </pre>
     * <p>
     * The return value is currently ignored by Mouse Tweaks.
     *
     * @return True if quick crafting was disabled.
     */
    boolean MT_disableRMBDraggingFunctionality();

    /**
     * Click the given slot.
     * <p>
     * For vanilla containers this method looks like this:
     * <pre>
     * this.slotClicked(slot,
     *                  slot.index,
     *                  mouseButton,
     *                  clickType);
     * </pre>
     *
     * @param slot        the slot to click
     * @param mouseButton the mouse button to click, left is 0 and right is 1
     * @param clickType   click type, most frequently used ones are PICKUP (normal left or right click)
     *                    and QUICK_MOVE (shift click)
     */
    void MT_clickSlot(Slot slot, int mouseButton, ClickType clickType);
}
