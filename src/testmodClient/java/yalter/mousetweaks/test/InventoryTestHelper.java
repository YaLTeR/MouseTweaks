package yalter.mousetweaks.test;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import yalter.mousetweaks.mixin.AbstractContainerScreenAccessor;

public class InventoryTestHelper {

    public static final int GUI_SCALE = 2;

    /**
     * Moves the cursor to the center of a slot.
     */
    public static void setCursorToSlot(ClientGameTestContext context, AbstractContainerScreen<?> screen, Slot slot) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        int leftPos = accessor.mousetweaks$getLeftPos();
        int topPos = accessor.mousetweaks$getTopPos();
        double x = (leftPos + slot.x + 8) * GUI_SCALE;
        double y = (topPos + slot.y + 8) * GUI_SCALE;
        context.getInput().setCursorPos(x, y);
        context.waitTick();
    }

    /**
     * Asserts that a slot contains the expected item and count.
     */
    public static void assertSlotContains(Slot slot, Item expectedItem, int expectedCount) {
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) {
            if (expectedCount != 0) {
                throw new AssertionError("Expected slot " + slot.index + " to contain " + expectedCount + " " + expectedItem + " but it was empty");
            }
            return;
        }

        if (!stack.is(expectedItem)) {
            throw new AssertionError("Expected slot " + slot.index + " to contain " + expectedItem + " but it contained " + stack.getItem());
        }

        if (stack.getCount() != expectedCount) {
            throw new AssertionError("Expected slot " + slot.index + " to contain " + expectedCount + " items but it contained " + stack.getCount());
        }
    }

    /**
     * Asserts that a slot is empty.
     */
    public static void assertSlotEmpty(Slot slot) {
        if (!slot.getItem().isEmpty()) {
            throw new AssertionError("Expected slot " + slot.index + " to be empty but it contained " + slot.getItem());
        }
    }

    /**
     * Asserts that a slot is not empty.
     */
    public static void assertSlotNotEmpty(Slot slot) {
        if (slot.getItem().isEmpty()) {
            throw new AssertionError("Expected slot " + slot.index + " to be non-empty but it was empty");
        }
    }

    /**
     * Asserts that the carried item matches expectations.
     */
    public static void assertCarried(Minecraft mc, Item expectedItem, int expectedCount) {
        ItemStack carried = mc.player.containerMenu.getCarried();
        if (carried.isEmpty()) {
            if (expectedCount != 0) {
                throw new AssertionError("Expected carried item to be " + expectedCount + " " + expectedItem + " but nothing was carried");
            }
            return;
        }

        if (!carried.is(expectedItem)) {
            throw new AssertionError("Expected carried item to be " + expectedItem + " but it was " + carried.getItem());
        }

        if (carried.getCount() != expectedCount) {
            throw new AssertionError("Expected carried count to be " + expectedCount + " but it was " + carried.getCount());
        }
    }

    /**
     * Asserts that no item is being carried.
     */
    public static void assertCarriedEmpty(Minecraft mc) {
        ItemStack carried = mc.player.containerMenu.getCarried();
        if (!carried.isEmpty()) {
            throw new AssertionError("Expected no carried item but carried " + carried);
        }
    }
}
