package yalter.mousetweaks.test;

import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import yalter.mousetweaks.mixin.AbstractContainerScreenAccessor;

import static yalter.mousetweaks.test.InventoryTestHelper.*;

@SuppressWarnings("UnstableApiUsage")
class InventoryTests {

    final ClientGameTestContext context;
    final TestSingleplayerContext world;
    AbstractContainerScreen<?> screen;

    InventoryTests(ClientGameTestContext context, TestSingleplayerContext world) {
        this.context = context;
        this.world = world;
    }

    // ---- Helpers ----

    private Slot getSlot(int index) {
        return screen.getMenu().slots.get(index);
    }

    /**
     * Moves the cursor to the center of a slot.
     */
    void setCursorTo(Slot slot) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        int leftPos = accessor.mousetweaks$getLeftPos();
        int topPos = accessor.mousetweaks$getTopPos();
        double x = (leftPos + slot.x + 8) * GUI_SCALE;
        double y = (topPos + slot.y + 8) * GUI_SCALE;
        context.getInput().setCursorPos(x, y);
        context.waitTick();
    }

    /**
     * Moves the cursor to a slot, left-clicks it, and waits a tick.
     */
    void leftClick(Slot slot) {
        setCursorTo(slot);
        context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
    }

    /**
     * Opens the player inventory, storing the screen.
     */
    void openPlayerInventory() {
        context.getInput().pressKey(options -> options.keyInventory);
        context.waitForScreen(AbstractContainerScreen.class);
        screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
    }

    /**
     * Places a block at 0 ~ 1 (clearing any existing block), looks at it, and opens it,
     * storing the screen.
     */
    void placeAndOpenBlock(String blockId) {
        // Set block to air first to remove the previous one so its contents don't move over.
        world.getServer().runCommand("setblock 0 ~ 1 air");
        world.getServer().runCommand("setblock 0 ~ 1 minecraft:" + blockId);
        context.waitTick();
        context.runOnClient(mc -> mc.player.setXRot(45));
        context.waitTick();
        context.getInput().pressKey(options -> options.keyUse);
        context.waitForScreen(AbstractContainerScreen.class);
        screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
    }

    /**
     * Closes the current screen.
     */
    void closeScreen() {
        screen = null;
        context.setScreen(() -> null);
        context.waitTick();
    }

    /**
     * Asserts that the carried item matches expectations.
     */
    void assertCarried(Item expectedItem, int expectedCount) {
        context.runOnClient(mc -> InventoryTestHelper.assertCarried(mc, expectedItem, expectedCount));
    }

    /**
     * Asserts that no item is being carried.
     */
    void assertCarriedEmpty() {
        context.runOnClient(InventoryTestHelper::assertCarriedEmpty);
    }

    // ---- Tests ----

    /**
     * Tests the RMB tweak: right-click drag distributes one item per slot per pass.
     * Circling over 4 slots twice should result in 2 items per slot.
     */
    void testRmbTweak() {
        // Clear inventory and give 64 stone
        world.getServer().runCommand("clear @p");
        world.getServer().runCommand("give @p minecraft:stone 64");
        context.waitTick();

        openPlayerInventory();
        TestInput input = context.getInput();

        // Find the slot with stone (hotbar slot 0 -> slot index 36 in InventoryMenu)
        Slot sourceSlot = getSlot(36);

        // Pick up the stack with left click
        leftClick(sourceSlot);

        assertCarried(Items.STONE, 64);

        // Get 4 empty slots in the main inventory area (slots 9-12)
        Slot slot0 = getSlot(9);
        Slot slot1 = getSlot(10);
        Slot slot2 = getSlot(11);
        Slot slot3 = getSlot(12);

        // RMB drag: circle over 4 slots twice
        // First pass
        setCursorTo(slot0);
        input.holdMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        context.waitTick();

        setCursorTo(slot1);
        setCursorTo(slot2);
        setCursorTo(slot3);

        // Second pass (circle back)
        setCursorTo(slot0);
        setCursorTo(slot1);
        setCursorTo(slot2);
        setCursorTo(slot3);

        input.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        context.waitTick();

        // Verify each slot got exactly 2 items (one per pass)
        assertSlotContains(slot0, Items.STONE, 2);
        assertSlotContains(slot1, Items.STONE, 2);
        assertSlotContains(slot2, Items.STONE, 2);
        assertSlotContains(slot3, Items.STONE, 2);

        // Verify remaining items on cursor (64 - 8 = 56)
        assertCarried(Items.STONE, 56);

        // Put items back by clicking on an empty slot
        Slot emptySlot = getSlot(13);
        leftClick(emptySlot);

        closeScreen();
    }

    /**
     * Tests the LMB tweak with item: left-drag with held item collects only compatible items.
     * Should NOT pick up items of a different type.
     */
    void testLmbTweakWithItem() {
        // Clear inventory and set up test scenario: multiple stacks of same and different items
        world.getServer().runCommand("clear @p");

        // Give items to specific slots
        // Main inventory: 2 stacks of cobblestone + 1 stack of dirt (different type) in between
        world.getServer().runCommand("item replace entity @p inventory.0 with minecraft:cobblestone 10");
        world.getServer().runCommand("item replace entity @p inventory.1 with minecraft:dirt 10");
        world.getServer().runCommand("item replace entity @p inventory.2 with minecraft:cobblestone 10");
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:cobblestone 5");
        context.waitTick();

        openPlayerInventory();
        TestInput input = context.getInput();

        // Pick up the stack from hotbar slot 0
        Slot sourceSlot = getSlot(36);
        setCursorTo(sourceSlot);
        input.holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Verify we picked up 5 cobblestone
        assertCarried(Items.COBBLESTONE, 5);

        Slot invSlot0 = getSlot(9);  // cobblestone
        Slot invSlot1 = getSlot(10); // dirt
        Slot invSlot2 = getSlot(11); // cobblestone

        // LMB drag across all three slots
        setCursorTo(invSlot0);
        setCursorTo(invSlot1);
        setCursorTo(invSlot2);
        input.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Only cobblestone should be collected (5 + 10 + 10 = 25)
        // Dirt should still be in its slot
        assertCarried(Items.COBBLESTONE, 25);

        // The cobblestone slots should be empty, but dirt should remain
        assertSlotEmpty(invSlot0);
        assertSlotContains(invSlot1, Items.DIRT, 10);
        assertSlotEmpty(invSlot2);

        // Put items back
        leftClick(sourceSlot);

        closeScreen();
    }

    /**
     * Tests the LMB tweak without item: shift+left-drag moves items to other inventory.
     */
    void testLmbTweakWithoutItem() {
        world.getServer().runCommand("clear @p");

        // Put items in main inventory slots
        world.getServer().runCommand("item replace entity @p inventory.0 with minecraft:dirt 16");
        world.getServer().runCommand("item replace entity @p inventory.1 with minecraft:cobblestone 16");
        world.getServer().runCommand("item replace entity @p inventory.2 with minecraft:dirt 16");
        context.waitTick();

        openPlayerInventory();
        TestInput input = context.getInput();

        // Get the inventory slots (inventory.0-2 -> slots 9-11 in menu)
        Slot invSlot0 = getSlot(9);
        Slot invSlot1 = getSlot(10);
        Slot invSlot2 = getSlot(11);

        Slot hotbarSlot0 = getSlot(36);
        Slot hotbarSlot1 = getSlot(37);

        // Shift+LMB drag across slots without holding an item
        input.holdShift();

        // For some reason, pressing Shift+LMB on a slot with item still picks it up?
        // As a workaround, press on en empty slot.
        setCursorTo(hotbarSlot0);
        input.holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
        assertCarriedEmpty();

        setCursorTo(invSlot0);
        setCursorTo(invSlot1);
        setCursorTo(invSlot2);

        input.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        input.releaseShift();
        context.waitTick();

        // The inventory slots should be empty (items moved to hotbar)
        assertSlotEmpty(invSlot0);
        assertSlotEmpty(invSlot1);
        assertSlotEmpty(invSlot2);

        // Hotbar slots should have the items (shift-click moves to hotbar)
        assertSlotContains(hotbarSlot0, Items.DIRT, 32);
        assertSlotContains(hotbarSlot1, Items.COBBLESTONE, 16);

        // Cursor should be empty
        assertCarriedEmpty();

        closeScreen();
    }

    /**
     * Tests the wheel tweak: scroll over slot moves items between inventories.
     */
    void testWheelTweak() {
        world.getServer().runCommand("clear @p");

        placeAndOpenBlock("chest");
        TestInput input = context.getInput();

        // Put items in the chest and player hotbar
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:gold_ingot 32");
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:iron_ingot 16");
        context.waitTick();

        // In a chest GUI: slots 0-26 are chest, 27-53 are player inventory, 54-62 are hotbar
        Slot chestSlot = getSlot(0);
        Slot chestSlot2 = getSlot(1);
        Slot invSlot1 = getSlot(27);

        // Verify the chest slot has gold ingots
        assertSlotContains(chestSlot, Items.GOLD_INGOT, 32);
        assertSlotEmpty(invSlot1);

        setCursorTo(chestSlot);

        // Scroll to push items from chest to player inventory
        input.scroll(-1);
        context.waitTick();

        // After one scroll, one item should have moved to player inventory
        assertSlotContains(chestSlot, Items.GOLD_INGOT, 31);
        assertSlotContains(invSlot1, Items.GOLD_INGOT, 1);

        // Scroll by 3 in one tick
        input.scroll(-3);
        context.waitTick();

        assertSlotContains(chestSlot, Items.GOLD_INGOT, 28);
        assertSlotContains(invSlot1, Items.GOLD_INGOT, 4);

        // Test scrolling from player inventory to chest
        Slot playerSlot = getSlot(54); // First hotbar slot
        assertSlotContains(playerSlot, Items.IRON_INGOT, 16);
        assertSlotEmpty(chestSlot2);

        setCursorTo(playerSlot);

        // Scroll to push items from player to chest
        input.scroll(-1);
        context.waitTick();

        // One iron ingot should have moved to the chest
        assertSlotContains(playerSlot, Items.IRON_INGOT, 15);
        assertSlotContains(chestSlot2, Items.IRON_INGOT, 1);

        // Test scrolling to pull from player to chest
        setCursorTo(chestSlot2);
        input.scroll(2);
        context.waitTick();

        assertSlotContains(playerSlot, Items.IRON_INGOT, 13);
        assertSlotContains(chestSlot2, Items.IRON_INGOT, 3);

        closeScreen();
    }

    /**
     * Tests edge cases of the wheel tweak when pushing items.
     */
    void testWheelTweakPushEdgeCases() {
        world.getServer().runCommand("clear @p");

        placeAndOpenBlock("chest");
        TestInput input = context.getInput();

        // In a chest GUI: slots 0-26 are chest, 27-53 are player inventory, 54-62 are hotbar
        Slot chestSlot0 = getSlot(0);
        Slot chestSlot1 = getSlot(1);
        Slot chestSlot2 = getSlot(2);
        Slot invSlot1 = getSlot(27);
        Slot invSlot2 = getSlot(28);
        Slot hotbarSlot = getSlot(54);

        // === Test 1: Stack of 2, right-click picks 1, deposit 1, no final click needed ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:gold_ingot 2");
        context.waitTick();

        assertSlotContains(chestSlot0, Items.GOLD_INGOT, 2);

        setCursorTo(chestSlot0);
        input.scroll(-1);
        context.waitTick();

        // Right-click picked up ceil(2/2)=1, deposited 1, no final click. Chest keeps 1.
        assertSlotContains(chestSlot0, Items.GOLD_INGOT, 1);
        assertSlotContains(invSlot1, Items.GOLD_INGOT, 1);
        assertCarriedEmpty();

        // === Test 2: Stack of 3, right-click picks 2, deposit 1, final click puts back 1 ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.1 with minecraft:iron_ingot 3");
        context.waitTick();

        assertSlotContains(chestSlot1, Items.IRON_INGOT, 3);

        setCursorTo(chestSlot1);
        input.scroll(-1);
        context.waitTick();

        // Right-click picked up ceil(3/2)=2, deposited 1, final click put back 1. Chest keeps 2.
        assertSlotContains(chestSlot1, Items.IRON_INGOT, 2);
        assertSlotContains(invSlot2, Items.IRON_INGOT, 1);
        assertCarriedEmpty();

        // === Test 3: Holding a different item on mouse, scroll, verify it's restored ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.2 with minecraft:gold_ingot 8");
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:diamond 5");
        context.waitTick();

        leftClick(hotbarSlot);
        assertCarried(Items.DIAMOND, 5);

        // Scroll to push 1 gold from chest while holding diamonds.
        setCursorTo(chestSlot2);
        input.scroll(-1);
        context.waitTick();

        // Gold should have moved, and diamonds should still be on the cursor.
        assertSlotContains(chestSlot2, Items.GOLD_INGOT, 7);
        assertCarried(Items.DIAMOND, 5);

        // === Test 4: Holding item on cursor, scroll over stack of 1 (final click should be left, not right) ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.3 with minecraft:emerald 1");
        context.waitTick();

        Slot chestSlot3 = getSlot(3);
        Slot invSlot3 = getSlot(29);
        assertSlotContains(chestSlot3, Items.EMERALD, 1);

        // Scroll to push 1 emerald from chest while holding diamonds.
        setCursorTo(chestSlot3);
        input.scroll(-1);
        context.waitTick();

        // Left-click picked up 1, deposited 1, no final click needed. Diamonds restored on cursor.
        assertSlotEmpty(chestSlot3);
        assertSlotContains(invSlot3, Items.EMERALD, 1);
        assertCarried(Items.DIAMOND, 5);

        // Put diamonds back.
        leftClick(hotbarSlot);

        // === Test 5: Push 2 items into an empty inventory slot ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.4 with minecraft:copper_ingot 4");
        context.waitTick();

        Slot chestSlot4 = getSlot(4);
        Slot invSlot4 = getSlot(30);
        assertSlotContains(chestSlot4, Items.COPPER_INGOT, 4);

        // Scroll -2 to push 2 copper. Right-click picks up ceil(4/2)=2, both should be deposited.
        setCursorTo(chestSlot4);
        input.scroll(-2);
        context.waitTick();

        assertSlotContains(chestSlot4, Items.COPPER_INGOT, 2);
        assertSlotContains(invSlot4, Items.COPPER_INGOT, 2);
        assertCarriedEmpty();

        // === Test 6: Stack of 2, two scroll ticks, left-click should pick up all 2 ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.5 with minecraft:lapis_lazuli 2");
        context.waitTick();

        Slot chestSlot5 = getSlot(5);
        Slot invSlot5 = getSlot(31);
        assertSlotContains(chestSlot5, Items.LAPIS_LAZULI, 2);

        // Scroll -2 to push 2 lapis. With left-click, all 2 are picked up and deposited.
        setCursorTo(chestSlot5);
        input.scroll(-2);
        context.waitTick();

        assertSlotEmpty(chestSlot5);
        assertSlotContains(invSlot5, Items.LAPIS_LAZULI, 2);
        assertCarriedEmpty();

        closeScreen();
    }

    /**
     * Tests edge cases of the wheel tweak when pulling items.
     */
    void testWheelTweakPullEdgeCases() {
        world.getServer().runCommand("clear @p");

        placeAndOpenBlock("chest");
        TestInput input = context.getInput();

        // In a chest GUI: slots 0-26 are chest, 27-53 are player inventory, 54-62 are hotbar
        Slot chestSlot0 = getSlot(0);
        Slot chestSlot1 = getSlot(1);
        Slot invSlot1 = getSlot(27);
        Slot invSlot2 = getSlot(28);
        Slot hotbarSlot = getSlot(54);

        // === Test 1: Pull from stack of 2, right-click picks 1, deposit 1, no final click needed ===
        world.getServer().runCommand("item replace entity @p inventory.0 with minecraft:gold_ingot 1");
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:gold_ingot 2");
        context.waitTick();

        assertSlotContains(invSlot1, Items.GOLD_INGOT, 1);
        assertSlotContains(chestSlot0, Items.GOLD_INGOT, 2);

        // Scroll to pull 1 gold from chest into player inventory slot
        setCursorTo(invSlot1);
        input.scroll(1);
        context.waitTick();

        // Right-click picked up ceil(2/2)=1 from chest, deposited 1 into invSlot1, no final click.
        assertSlotContains(chestSlot0, Items.GOLD_INGOT, 1);
        assertSlotContains(invSlot1, Items.GOLD_INGOT, 2);
        assertCarriedEmpty();

        // === Test 2: Pull from stack of 3, right-click picks 2, deposit 1, final click puts back 1 ===
        world.getServer().runCommand("item replace entity @p inventory.1 with minecraft:iron_ingot 1");
        world.getServer().runCommand("item replace block 0 ~ 1 container.1 with minecraft:iron_ingot 3");
        context.waitTick();

        assertSlotContains(invSlot2, Items.IRON_INGOT, 1);
        assertSlotContains(chestSlot1, Items.IRON_INGOT, 3);

        // Scroll to pull 1 iron from chest into player inventory slot
        setCursorTo(invSlot2);
        input.scroll(1);
        context.waitTick();

        // Right-click picked up ceil(3/2)=2, deposited 1, final click put back 1.
        assertSlotContains(chestSlot1, Items.IRON_INGOT, 2);
        assertSlotContains(invSlot2, Items.IRON_INGOT, 2);
        assertCarriedEmpty();

        // === Test 3: Pull while holding a different item on mouse, source stack of 8 ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.2 with minecraft:copper_ingot 8");
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:diamond 5");
        context.waitTick();

        Slot chestSlot2 = getSlot(2);
        Slot invSlot3 = getSlot(29);

        leftClick(hotbarSlot);
        assertCarried(Items.DIAMOND, 5);

        // Put 1 copper in the target slot so pull finds a compatible source.
        world.getServer().runCommand("item replace entity @p inventory.2 with minecraft:copper_ingot 1");
        context.waitTick();

        // Scroll to pull 1 copper from chest while holding diamonds.
        setCursorTo(invSlot3);
        input.scroll(1);
        context.waitTick();

        // Copper should have moved, and diamonds should still be on the cursor.
        assertSlotContains(chestSlot2, Items.COPPER_INGOT, 7);
        assertSlotContains(invSlot3, Items.COPPER_INGOT, 2);
        assertCarried(Items.DIAMOND, 5);

        // === Test 4: Pull while holding item on cursor, source stack of 1 (left-click pickup) ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.3 with minecraft:emerald 1");
        context.waitTick();

        Slot chestSlot3 = getSlot(3);
        Slot invSlot4 = getSlot(30);

        // Put 1 emerald in the target slot so pull finds a compatible source.
        world.getServer().runCommand("item replace entity @p inventory.3 with minecraft:emerald 1");
        context.waitTick();

        // Scroll to pull 1 emerald from chest while holding diamonds.
        setCursorTo(invSlot4);
        input.scroll(1);
        context.waitTick();

        // Left-click picked up 1, deposited 1, no final click needed. Diamonds restored on cursor.
        assertSlotEmpty(chestSlot3);
        assertSlotContains(invSlot4, Items.EMERALD, 2);
        assertCarried(Items.DIAMOND, 5);

        // Put diamonds back.
        leftClick(hotbarSlot);

        closeScreen();
    }

    /**
     * Tests the wheel tweak while holding a bundle on the cursor.
     * Scrolling over a non-bundle item should move it normally, and the bundle should remain on the cursor.
     */
    void testWheelTweakWithBundle() {
        world.getServer().runCommand("clear @p");

        placeAndOpenBlock("chest");
        TestInput input = context.getInput();

        // Put items in the chest and a bundle in the hotbar
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:gold_ingot 8");
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:bundle[minecraft:bundle_contents=[{id:\"minecraft:stick\",count:2}]]");
        context.waitTick();

        Slot chestSlot = getSlot(0);
        Slot hotbarSlot = getSlot(54);
        Slot invSlot = getSlot(27);

        // Pick up the bundle (contains 2 sticks)
        leftClick(hotbarSlot);
        assertCarried(Items.BUNDLE, 1);

        // Scroll to push 1 gold from chest while holding the bundle
        setCursorTo(chestSlot);
        input.scroll(-1);
        context.waitTick();

        // Gold should have moved, and the bundle should still be on the cursor
        assertSlotContains(chestSlot, Items.GOLD_INGOT, 7);
        assertSlotContains(invSlot, Items.GOLD_INGOT, 1);
        assertCarried(Items.BUNDLE, 1);

        // === Pull: scroll to pull 1 gold from chest into player inventory slot while holding bundle ===
        setCursorTo(invSlot);
        input.scroll(1);
        context.waitTick();

        // Another gold should have moved, and the bundle should still be on the cursor
        assertSlotContains(chestSlot, Items.GOLD_INGOT, 6);
        assertSlotContains(invSlot, Items.GOLD_INGOT, 2);
        assertCarried(Items.BUNDLE, 1);

        // === Push with bundle on cursor, target slot has only 1 item (final click must be left, not right) ===
        Slot chestSlot2 = getSlot(1);
        Slot invSlot2 = getSlot(28);
        world.getServer().runCommand("item replace block 0 ~ 1 container.1 with minecraft:emerald 1");
        context.waitTick();

        setCursorTo(chestSlot2);
        input.scroll(-1);
        context.waitTick();

        assertSlotEmpty(chestSlot2);
        assertSlotContains(invSlot2, Items.EMERALD, 1);
        assertCarried(Items.BUNDLE, 1);

        // === Pull with bundle on cursor, source slot has only 1 item (final click must be left, not right) ===
        Slot chestSlot3 = getSlot(2);
        Slot invSlot3 = getSlot(29);
        world.getServer().runCommand("item replace block 0 ~ 1 container.2 with minecraft:iron_ingot 1");
        world.getServer().runCommand("item replace entity @p inventory.2 with minecraft:iron_ingot 1");
        context.waitTick();

        setCursorTo(invSlot3);
        input.scroll(1);
        context.waitTick();

        assertSlotEmpty(chestSlot3);
        assertSlotContains(invSlot3, Items.IRON_INGOT, 2);
        assertCarried(Items.BUNDLE, 1);

        // Put the bundle back
        leftClick(hotbarSlot);

        closeScreen();
    }

    /**
     * Tests crafting output slot behaviors with the wheel tweak and LMB tweak.
     * Uses a crafting table with oak logs -> oak planks (1 log = 4 planks).
     */
    void testCraftingOutputSlot() {
        world.getServer().runCommand("clear @p");

        placeAndOpenBlock("crafting_table");
        TestInput input = context.getInput();

        // Give player oak logs for crafting planks (1 log = 4 planks)
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:oak_log 10");
        context.waitTick();

        // Slot indices for crafting table:
        // 0: output, 1-9: crafting grid (3x3), 10-36: player inventory, 37-45: hotbar
        Slot outputSlot = getSlot(0);
        Slot craftingSlot = getSlot(1);
        Slot invSlot = getSlot(10);
        Slot hotbarSlot = getSlot(37);

        // Place all logs in crafting slot
        leftClick(hotbarSlot);
        leftClick(craftingSlot);

        // Verify setup: crafting has logs, output shows 4 planks
        assertSlotContains(craftingSlot, Items.OAK_LOG, 10);
        assertSlotContains(outputSlot, Items.OAK_PLANKS, 4);
        assertCarriedEmpty();

        // === Test 1: Scroll from crafting output (push) crafts 1 batch, leaves nothing on mouse ===
        setCursorTo(outputSlot);
        input.scroll(-1);
        context.waitTick();

        // Crafting consumed 1 log, planks distributed to inventory
        assertSlotContains(craftingSlot, Items.OAK_LOG, 9);
        assertSlotContains(invSlot, Items.OAK_PLANKS, 4);
        assertCarriedEmpty();

        // Wait for the crafting output to appear again
        context.waitTick();
        assertSlotContains(outputSlot, Items.OAK_PLANKS, 4);

        // === Test 2: Scroll to pull from crafting output into compatible slot ===
        setCursorTo(invSlot);
        input.scroll(1);
        context.waitTick();

        // Crafting consumed 1 log, planks pulled into invSlot
        assertSlotContains(craftingSlot, Items.OAK_LOG, 8);
        assertSlotContains(invSlot, Items.OAK_PLANKS, 8); // 4 + 4
        assertCarriedEmpty();

        // Wait for the crafting output to appear again
        context.waitTick();
        assertSlotContains(outputSlot, Items.OAK_PLANKS, 4);

        // === Test 3: LMB drag with same item over crafting output ===
        // Start from invSlot which has planks
        setCursorTo(invSlot);
        input.holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        assertCarried(Items.OAK_PLANKS, 8);

        // Drag to crafting output
        setCursorTo(outputSlot);

        // Crafting consumed 1 log, planks added to mouse (8 + 4 = 12)
        assertSlotContains(craftingSlot, Items.OAK_LOG, 7);
        assertCarried(Items.OAK_PLANKS, 12);

        // Wait for the crafting output to appear again
        context.waitTick();
        assertSlotContains(outputSlot, Items.OAK_PLANKS, 4);

        input.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Clean up: put items in an empty slot
        leftClick(invSlot);

        closeScreen();
    }

    /**
     * Tests that scrolling to pull items from a furnace input slot does not reset the smelting progress.
     */
    void testFurnaceSmeltingPreserved() {
        world.getServer().runCommand("clear @p");

        placeAndOpenBlock("furnace");
        TestInput input = context.getInput();

        // Set up smelting: raw iron in input, coal as fuel.
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:raw_iron 8");
        world.getServer().runCommand("item replace block 0 ~ 1 container.1 with minecraft:coal 8");

        // Fast-forward cooking progress to near completion (190 of 200 ticks).
        world.getServer().runCommand("data merge block 0 ~ 1 {cooking_time_spent:190,cooking_total_time:200}");
        context.waitTick();

        // Furnace slots: 0=input, 1=fuel, 2=output.
        // Player inventory: 3-29=main, 30-38=hotbar.
        Slot inputSlot = getSlot(0);
        Slot outputSlot = getSlot(2);
        Slot playerSlot = getSlot(3);

        // Verify no output has been produced yet.
        assertSlotEmpty(outputSlot);

        // Scroll to push 1 item from furnace input to player inventory.
        setCursorTo(inputSlot);
        input.scroll(-1);
        context.waitTick();

        // Verify items moved correctly: 7 remain in furnace, 1 in player inventory.
        assertSlotContains(inputSlot, Items.RAW_IRON, 7);
        assertSlotContains(playerSlot, Items.RAW_IRON, 1);
        assertCarriedEmpty();

        // Wait for smelting to complete. If cooking continued from 190/200, it needs ~10 more ticks.
        // If cooking was reset to 0, it would only be at ~10/200 after this wait.
        context.waitTicks(10);

        // If smelting was preserved, an iron ingot should be in the output.
        assertSlotContains(outputSlot, Items.IRON_INGOT, 1);

        // === Test 2: Pull direction (scroll to pull from furnace input into player slot) ===

        // Fast-forward cooking progress again.
        world.getServer().runCommand("data merge block 0 ~ 1 {cooking_time_spent:190,cooking_total_time:200}");
        context.waitTick();

        // playerSlot now has 1 raw_iron (from the push test). Scroll to pull 1 more.
        setCursorTo(playerSlot);
        input.scroll(1);
        context.waitTick();

        // Verify: input has 5 (was 6 after first smelt consumed 1, now -1 more), player has 2.
        assertSlotContains(inputSlot, Items.RAW_IRON, 5);
        assertSlotContains(playerSlot, Items.RAW_IRON, 2);
        assertCarriedEmpty();

        // Wait for smelting to complete.
        context.waitTicks(10);

        // If smelting was preserved, another iron ingot should be in the output.
        assertSlotContains(outputSlot, Items.IRON_INGOT, 2);

        closeScreen();
    }
}
