package yalter.mousetweaks.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.Difficulty;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import yalter.mousetweaks.ConfigScreen;

import static yalter.mousetweaks.test.InventoryTestHelper.*;

@SuppressWarnings("UnstableApiUsage")
public class MouseTweaksClientGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        // Set GUI scale for consistent coordinate calculation
        context.runOnClient(mc -> {
            mc.options.guiScale().set(GUI_SCALE);
        });

        testOptionsScreenshot(context);

        try (TestSingleplayerContext world = context.worldBuilder()
                .adjustSettings(creator -> {
                    creator.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
                    creator.setDifficulty(Difficulty.PEACEFUL);
                })
                .create()) {

            world.getServer().runCommand("tp @p 0 ~ 0");
            world.getClientLevel().waitForChunksDownload();

            testRmbTweak(context, world);
            testLmbTweakWithItem(context, world);
            testLmbTweakWithoutItem(context, world);
            testWheelTweak(context, world);
            testWheelTweakPushEdgeCases(context, world);
            testWheelTweakPullEdgeCases(context, world);
            testWheelTweakWithBundle(context, world);
            testCraftingOutputSlot(context, world);
            testFurnaceSmeltingPreserved(context, world);
        }
    }

    /**
     * Opens the Mouse Tweaks options screen and takes a screenshot.
     */
    private void testOptionsScreenshot(ClientGameTestContext context) {
        context.setScreen(() -> new ConfigScreen(null));
        context.waitForScreen(ConfigScreen.class);

        context.takeScreenshot("mousetweaks_options");

        context.setScreen(() -> null);
    }

    /**
     * Tests the RMB tweak: right-click drag distributes one item per slot per pass.
     * Circling over 4 slots twice should result in 2 items per slot.
     */
    private void testRmbTweak(ClientGameTestContext context, TestSingleplayerContext world) {
        // Clear inventory and give 64 stone
        world.getServer().runCommand("clear @p");
        world.getServer().runCommand("give @p minecraft:stone 64");
        context.waitTick();

        // Open inventory
        context.getInput().pressKey(options -> options.keyInventory);
        context.waitForScreen(AbstractContainerScreen.class);

        AbstractContainerScreen<?> screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
        TestInput input = context.getInput();

        // Find the slot with stone (hotbar slot 0 -> slot index 36 in InventoryMenu)
        Slot sourceSlot = screen.getMenu().slots.get(36);

        // Pick up the stack with left click
        setCursorToSlot(context, screen, sourceSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Verify we picked up the items
        context.runOnClient(mc -> assertCarried(mc, Items.STONE, 64));

        // Get 4 empty slots in the main inventory area (slots 9-12)
        Slot slot0 = screen.getMenu().slots.get(9);
        Slot slot1 = screen.getMenu().slots.get(10);
        Slot slot2 = screen.getMenu().slots.get(11);
        Slot slot3 = screen.getMenu().slots.get(12);

        // RMB drag: circle over 4 slots twice
        // First pass
        setCursorToSlot(context, screen, slot0);
        input.holdMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        context.waitTick();

        setCursorToSlot(context, screen, slot1);
        setCursorToSlot(context, screen, slot2);
        setCursorToSlot(context, screen, slot3);

        // Second pass (circle back)
        setCursorToSlot(context, screen, slot0);
        setCursorToSlot(context, screen, slot1);
        setCursorToSlot(context, screen, slot2);
        setCursorToSlot(context, screen, slot3);

        input.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        context.waitTick();

        // Verify each slot got exactly 2 items (one per pass)
        assertSlotContains(slot0, Items.STONE, 2);
        assertSlotContains(slot1, Items.STONE, 2);
        assertSlotContains(slot2, Items.STONE, 2);
        assertSlotContains(slot3, Items.STONE, 2);

        // Verify remaining items on cursor (64 - 8 = 56)
        context.runOnClient(mc -> assertCarried(mc, Items.STONE, 56));

        // Put items back by clicking on an empty slot
        Slot emptySlot = screen.getMenu().slots.get(13);
        setCursorToSlot(context, screen, emptySlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Close inventory
        context.setScreen(() -> null);
        context.waitTick();
    }

    /**
     * Tests the LMB tweak with item: left-drag with held item collects only compatible items.
     * Should NOT pick up items of a different type.
     */
    private void testLmbTweakWithItem(ClientGameTestContext context, TestSingleplayerContext world) {
        // Clear inventory and set up test scenario: multiple stacks of same and different items
        world.getServer().runCommand("clear @p");

        // Give items to specific slots
        // Main inventory: 2 stacks of cobblestone + 1 stack of dirt (different type) in between
        world.getServer().runCommand("item replace entity @p inventory.0 with minecraft:cobblestone 10");
        world.getServer().runCommand("item replace entity @p inventory.1 with minecraft:dirt 10");
        world.getServer().runCommand("item replace entity @p inventory.2 with minecraft:cobblestone 10");
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:cobblestone 5");
        context.waitTick();

        // Open inventory
        context.getInput().pressKey(options -> options.keyInventory);
        context.waitForScreen(AbstractContainerScreen.class);

        AbstractContainerScreen<?> screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
        TestInput input = context.getInput();

        // Pick up the stack from hotbar slot 0
        Slot sourceSlot = screen.getMenu().slots.get(36);
        setCursorToSlot(context, screen, sourceSlot);
        input.holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Verify we picked up 5 cobblestone
        context.runOnClient(mc -> assertCarried(mc, Items.COBBLESTONE, 5));

        // Get the inventory slots
        Slot invSlot0 = screen.getMenu().slots.get(9);  // cobblestone
        Slot invSlot1 = screen.getMenu().slots.get(10); // dirt
        Slot invSlot2 = screen.getMenu().slots.get(11); // cobblestone

        // LMB drag across all three slots
        setCursorToSlot(context, screen, invSlot0);
        setCursorToSlot(context, screen, invSlot1);
        setCursorToSlot(context, screen, invSlot2);
        input.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Only cobblestone should be collected (5 + 10 + 10 = 25)
        // Dirt should still be in its slot
        context.runOnClient(mc -> assertCarried(mc, Items.COBBLESTONE, 25));

        // The cobblestone slots should be empty, but dirt should remain
        assertSlotEmpty(invSlot0);
        assertSlotContains(invSlot1, Items.DIRT, 10);
        assertSlotEmpty(invSlot2);

        // Put items back
        setCursorToSlot(context, screen, sourceSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Close inventory
        context.setScreen(() -> null);
        context.waitTick();
    }

    /**
     * Tests the LMB tweak without item: shift+left-drag moves items to other inventory.
     */
    private void testLmbTweakWithoutItem(ClientGameTestContext context, TestSingleplayerContext world) {
        // Clear inventory and set up test scenario
        world.getServer().runCommand("clear @p");

        // Put items in main inventory slots
        world.getServer().runCommand("item replace entity @p inventory.0 with minecraft:dirt 16");
        world.getServer().runCommand("item replace entity @p inventory.1 with minecraft:cobblestone 16");
        world.getServer().runCommand("item replace entity @p inventory.2 with minecraft:dirt 16");
        context.waitTick();

        // Open inventory
        context.getInput().pressKey(options -> options.keyInventory);
        context.waitForScreen(AbstractContainerScreen.class);

        AbstractContainerScreen<?> screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
        TestInput input = context.getInput();

        // Get the inventory slots (inventory.0-2 -> slots 9-11 in menu)
        Slot invSlot0 = screen.getMenu().slots.get(9);
        Slot invSlot1 = screen.getMenu().slots.get(10);
        Slot invSlot2 = screen.getMenu().slots.get(11);

        // Hotbar slots (for checking where items moved)
        Slot hotbarSlot0 = screen.getMenu().slots.get(36);
        Slot hotbarSlot1 = screen.getMenu().slots.get(37);

        // Shift+LMB drag across slots without holding an item
        input.holdShift();

        // For some reason, pressing Shift+LMB on a slot with item still picks it up?
        // As a workaround, press on en empty slot.
        setCursorToSlot(context, screen, hotbarSlot0);
        input.holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        setCursorToSlot(context, screen, invSlot0);
        setCursorToSlot(context, screen, invSlot1);
        setCursorToSlot(context, screen, invSlot2);

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
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // Close inventory
        context.setScreen(() -> null);
        context.waitTick();
    }

    /**
     * Tests the wheel tweak: scroll over slot moves items between inventories.
     */
    private void testWheelTweak(ClientGameTestContext context, TestSingleplayerContext world) {
        // Clear inventory
        world.getServer().runCommand("clear @p");

        // Place a chest in front of the player
        world.getServer().runCommand("setblock 0 ~ 1 minecraft:chest");
        context.waitTick();

        // Put items in the chest
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:gold_ingot 32");

        // Put items in player hotbar for testing reverse direction
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:iron_ingot 16");

        // Make player look at the chest
        context.runOnClient(mc -> mc.player.setXRot(45));
        context.waitTick();

        // Press use key to open the chest
        context.getInput().pressKey(options -> options.keyUse);
        context.waitForScreen(AbstractContainerScreen.class);

        AbstractContainerScreen<?> screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
        TestInput input = context.getInput();

        // In a chest GUI: slots 0-26 are chest, 27-53 are player inventory, 54-62 are hotbar
        Slot chestSlot = screen.getMenu().slots.get(0);
        Slot chestSlot2 = screen.getMenu().slots.get(1);
        Slot invSlot1 = screen.getMenu().slots.get(27);

        // Verify the chest slot has gold ingots
        assertSlotContains(chestSlot, Items.GOLD_INGOT, 32);
        assertSlotEmpty(invSlot1);

        // Move cursor over the chest slot
        setCursorToSlot(context, screen, chestSlot);

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
        Slot playerSlot = screen.getMenu().slots.get(54); // First hotbar slot
        assertSlotContains(playerSlot, Items.IRON_INGOT, 16);
        assertSlotEmpty(chestSlot2);

        setCursorToSlot(context, screen, playerSlot);

        // Scroll to push items from player to chest
        input.scroll(-1);
        context.waitTick();

        // One iron ingot should have moved to the chest
        assertSlotContains(playerSlot, Items.IRON_INGOT, 15);
        assertSlotContains(chestSlot2, Items.IRON_INGOT, 1);

        // Test scrolling to pull from player to chest
        setCursorToSlot(context, screen, chestSlot2);
        input.scroll(2);
        context.waitTick();

        assertSlotContains(playerSlot, Items.IRON_INGOT, 13);
        assertSlotContains(chestSlot2, Items.IRON_INGOT, 3);

        // Close the chest
        context.setScreen(() -> null);
        context.waitTick();
    }

    /**
     * Tests edge cases of the wheel tweak when pushing items.
     */
    private void testWheelTweakPushEdgeCases(ClientGameTestContext context, TestSingleplayerContext world) {
        // Clear inventory
        world.getServer().runCommand("clear @p");

        // Place a fresh chest (remove old one first to clear its contents)
        world.getServer().runCommand("setblock 0 ~ 1 air");
        world.getServer().runCommand("setblock 0 ~ 1 minecraft:chest");
        context.waitTick();

        // Make player look at the chest
        context.runOnClient(mc -> mc.player.setXRot(45));
        context.waitTick();

        // Open the chest
        context.getInput().pressKey(options -> options.keyUse);
        context.waitForScreen(AbstractContainerScreen.class);

        AbstractContainerScreen<?> screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
        TestInput input = context.getInput();

        // In a chest GUI: slots 0-26 are chest, 27-53 are player inventory, 54-62 are hotbar
        Slot chestSlot0 = screen.getMenu().slots.get(0);
        Slot chestSlot1 = screen.getMenu().slots.get(1);
        Slot chestSlot2 = screen.getMenu().slots.get(2);
        Slot invSlot1 = screen.getMenu().slots.get(27);
        Slot invSlot2 = screen.getMenu().slots.get(28);
        Slot hotbarSlot = screen.getMenu().slots.get(54);

        // === Test 1: Stack of 2, right-click picks 1, deposit 1, no final click needed ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:gold_ingot 2");
        context.waitTick();

        assertSlotContains(chestSlot0, Items.GOLD_INGOT, 2);

        setCursorToSlot(context, screen, chestSlot0);
        input.scroll(-1);
        context.waitTick();

        // Right-click picked up ceil(2/2)=1, deposited 1, no final click. Chest keeps 1.
        assertSlotContains(chestSlot0, Items.GOLD_INGOT, 1);
        assertSlotContains(invSlot1, Items.GOLD_INGOT, 1);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // === Test 2: Stack of 3, right-click picks 2, deposit 1, final click puts back 1 ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.1 with minecraft:iron_ingot 3");
        context.waitTick();

        assertSlotContains(chestSlot1, Items.IRON_INGOT, 3);

        setCursorToSlot(context, screen, chestSlot1);
        input.scroll(-1);
        context.waitTick();

        // Right-click picked up ceil(3/2)=2, deposited 1, final click put back 1. Chest keeps 2.
        assertSlotContains(chestSlot1, Items.IRON_INGOT, 2);
        assertSlotContains(invSlot2, Items.IRON_INGOT, 1);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // === Test 3: Holding a different item on mouse, scroll, verify it's restored ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.2 with minecraft:gold_ingot 8");
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:diamond 5");
        context.waitTick();

        // Pick up diamonds from hotbar to put them on the cursor.
        setCursorToSlot(context, screen, hotbarSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
        context.runOnClient(mc -> assertCarried(mc, Items.DIAMOND, 5));

        // Scroll to push 1 gold from chest while holding diamonds.
        setCursorToSlot(context, screen, chestSlot2);
        input.scroll(-1);
        context.waitTick();

        // Gold should have moved, and diamonds should still be on the cursor.
        assertSlotContains(chestSlot2, Items.GOLD_INGOT, 7);
        context.runOnClient(mc -> assertCarried(mc, Items.DIAMOND, 5));

        // === Test 4: Holding item on cursor, scroll over stack of 1 (final click should be left, not right) ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.3 with minecraft:emerald 1");
        context.waitTick();

        Slot chestSlot3 = screen.getMenu().slots.get(3);
        Slot invSlot3 = screen.getMenu().slots.get(29);
        assertSlotContains(chestSlot3, Items.EMERALD, 1);

        // Scroll to push 1 emerald from chest while holding diamonds.
        setCursorToSlot(context, screen, chestSlot3);
        input.scroll(-1);
        context.waitTick();

        // Left-click picked up 1, deposited 1, no final click needed. Diamonds restored on cursor.
        assertSlotEmpty(chestSlot3);
        assertSlotContains(invSlot3, Items.EMERALD, 1);
        context.runOnClient(mc -> assertCarried(mc, Items.DIAMOND, 5));

        // Put diamonds back.
        setCursorToSlot(context, screen, hotbarSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // === Test 5: Push 2 items into an empty inventory slot ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.4 with minecraft:copper_ingot 4");
        context.waitTick();

        Slot chestSlot4 = screen.getMenu().slots.get(4);
        Slot invSlot4 = screen.getMenu().slots.get(30);
        assertSlotContains(chestSlot4, Items.COPPER_INGOT, 4);

        // Scroll -2 to push 2 copper. Right-click picks up ceil(4/2)=2, both should be deposited.
        setCursorToSlot(context, screen, chestSlot4);
        input.scroll(-2);
        context.waitTick();

        assertSlotContains(chestSlot4, Items.COPPER_INGOT, 2);
        assertSlotContains(invSlot4, Items.COPPER_INGOT, 2);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // === Test 6: Stack of 2, two scroll ticks, left-click should pick up all 2 ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.5 with minecraft:lapis_lazuli 2");
        context.waitTick();

        Slot chestSlot5 = screen.getMenu().slots.get(5);
        Slot invSlot5 = screen.getMenu().slots.get(31);
        assertSlotContains(chestSlot5, Items.LAPIS_LAZULI, 2);

        // Scroll -2 to push 2 lapis. With left-click, all 2 are picked up and deposited.
        setCursorToSlot(context, screen, chestSlot5);
        input.scroll(-2);
        context.waitTick();

        assertSlotEmpty(chestSlot5);
        assertSlotContains(invSlot5, Items.LAPIS_LAZULI, 2);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // Close the chest
        context.setScreen(() -> null);
        context.waitTick();
    }

    /**
     * Tests edge cases of the wheel tweak when pulling items.
     */
    private void testWheelTweakPullEdgeCases(ClientGameTestContext context, TestSingleplayerContext world) {
        // Clear inventory
        world.getServer().runCommand("clear @p");

        // Place a fresh chest (remove old one first to clear its contents)
        world.getServer().runCommand("setblock 0 ~ 1 air");
        world.getServer().runCommand("setblock 0 ~ 1 minecraft:chest");
        context.waitTick();

        // Make player look at the chest
        context.runOnClient(mc -> mc.player.setXRot(45));
        context.waitTick();

        // Open the chest
        context.getInput().pressKey(options -> options.keyUse);
        context.waitForScreen(AbstractContainerScreen.class);

        AbstractContainerScreen<?> screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
        TestInput input = context.getInput();

        // In a chest GUI: slots 0-26 are chest, 27-53 are player inventory, 54-62 are hotbar
        Slot chestSlot0 = screen.getMenu().slots.get(0);
        Slot chestSlot1 = screen.getMenu().slots.get(1);
        Slot invSlot1 = screen.getMenu().slots.get(27);
        Slot invSlot2 = screen.getMenu().slots.get(28);
        Slot hotbarSlot = screen.getMenu().slots.get(54);

        // === Test 1: Pull from stack of 2, right-click picks 1, deposit 1, no final click needed ===
        world.getServer().runCommand("item replace entity @p inventory.0 with minecraft:gold_ingot 1");
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:gold_ingot 2");
        context.waitTick();

        assertSlotContains(invSlot1, Items.GOLD_INGOT, 1);
        assertSlotContains(chestSlot0, Items.GOLD_INGOT, 2);

        // Scroll to pull 1 gold from chest into player inventory slot
        setCursorToSlot(context, screen, invSlot1);
        input.scroll(1);
        context.waitTick();

        // Right-click picked up ceil(2/2)=1 from chest, deposited 1 into invSlot1, no final click.
        assertSlotContains(chestSlot0, Items.GOLD_INGOT, 1);
        assertSlotContains(invSlot1, Items.GOLD_INGOT, 2);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // === Test 2: Pull from stack of 3, right-click picks 2, deposit 1, final click puts back 1 ===
        world.getServer().runCommand("item replace entity @p inventory.1 with minecraft:iron_ingot 1");
        world.getServer().runCommand("item replace block 0 ~ 1 container.1 with minecraft:iron_ingot 3");
        context.waitTick();

        assertSlotContains(invSlot2, Items.IRON_INGOT, 1);
        assertSlotContains(chestSlot1, Items.IRON_INGOT, 3);

        // Scroll to pull 1 iron from chest into player inventory slot
        setCursorToSlot(context, screen, invSlot2);
        input.scroll(1);
        context.waitTick();

        // Right-click picked up ceil(3/2)=2, deposited 1, final click put back 1.
        assertSlotContains(chestSlot1, Items.IRON_INGOT, 2);
        assertSlotContains(invSlot2, Items.IRON_INGOT, 2);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // === Test 3: Pull while holding a different item on mouse, source stack of 8 ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.2 with minecraft:copper_ingot 8");
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:diamond 5");
        context.waitTick();

        Slot chestSlot2 = screen.getMenu().slots.get(2);
        Slot invSlot3 = screen.getMenu().slots.get(29);

        // Pick up diamonds from hotbar to put them on the cursor.
        setCursorToSlot(context, screen, hotbarSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
        context.runOnClient(mc -> assertCarried(mc, Items.DIAMOND, 5));

        // Put 1 copper in the target slot so pull finds a compatible source.
        world.getServer().runCommand("item replace entity @p inventory.2 with minecraft:copper_ingot 1");
        context.waitTick();

        // Scroll to pull 1 copper from chest while holding diamonds.
        setCursorToSlot(context, screen, invSlot3);
        input.scroll(1);
        context.waitTick();

        // Copper should have moved, and diamonds should still be on the cursor.
        assertSlotContains(chestSlot2, Items.COPPER_INGOT, 7);
        assertSlotContains(invSlot3, Items.COPPER_INGOT, 2);
        context.runOnClient(mc -> assertCarried(mc, Items.DIAMOND, 5));

        // === Test 4: Pull while holding item on cursor, source stack of 1 (left-click pickup) ===
        world.getServer().runCommand("item replace block 0 ~ 1 container.3 with minecraft:emerald 1");
        context.waitTick();

        Slot chestSlot3 = screen.getMenu().slots.get(3);
        Slot invSlot4 = screen.getMenu().slots.get(30);

        // Put 1 emerald in the target slot so pull finds a compatible source.
        world.getServer().runCommand("item replace entity @p inventory.3 with minecraft:emerald 1");
        context.waitTick();

        // Scroll to pull 1 emerald from chest while holding diamonds.
        setCursorToSlot(context, screen, invSlot4);
        input.scroll(1);
        context.waitTick();

        // Left-click picked up 1, deposited 1, no final click needed. Diamonds restored on cursor.
        assertSlotEmpty(chestSlot3);
        assertSlotContains(invSlot4, Items.EMERALD, 2);
        context.runOnClient(mc -> assertCarried(mc, Items.DIAMOND, 5));

        // Put diamonds back.
        setCursorToSlot(context, screen, hotbarSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Close the chest
        context.setScreen(() -> null);
        context.waitTick();
    }

    /**
     * Tests the wheel tweak while holding a bundle on the cursor.
     * Scrolling over a non-bundle item should move it normally, and the bundle should remain on the cursor.
     */
    private void testWheelTweakWithBundle(ClientGameTestContext context, TestSingleplayerContext world) {
        // Clear inventory
        world.getServer().runCommand("clear @p");

        // Place a fresh chest (remove old one first to clear its contents)
        world.getServer().runCommand("setblock 0 ~ 1 air");
        world.getServer().runCommand("setblock 0 ~ 1 minecraft:chest");
        context.waitTick();

        // Put items in the chest and a bundle in the hotbar
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:gold_ingot 8");
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:bundle[minecraft:bundle_contents=[{id:\"minecraft:stick\",count:2}]]");

        // Open the chest
        context.runOnClient(mc -> mc.player.setXRot(45));
        context.waitTick();
        context.getInput().pressKey(options -> options.keyUse);
        context.waitForScreen(AbstractContainerScreen.class);

        AbstractContainerScreen<?> screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
        TestInput input = context.getInput();

        Slot chestSlot = screen.getMenu().slots.get(0);
        Slot hotbarSlot = screen.getMenu().slots.get(54);
        Slot invSlot = screen.getMenu().slots.get(27);

        // Pick up the bundle (contains 2 sticks)
        setCursorToSlot(context, screen, hotbarSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
        context.runOnClient(mc -> assertCarried(mc, Items.BUNDLE, 1));

        // Scroll to push 1 gold from chest while holding the bundle
        setCursorToSlot(context, screen, chestSlot);
        input.scroll(-1);
        context.waitTick();

        // Gold should have moved, and the bundle should still be on the cursor
        assertSlotContains(chestSlot, Items.GOLD_INGOT, 7);
        assertSlotContains(invSlot, Items.GOLD_INGOT, 1);
        context.runOnClient(mc -> assertCarried(mc, Items.BUNDLE, 1));

        // === Pull: scroll to pull 1 gold from chest into player inventory slot while holding bundle ===
        setCursorToSlot(context, screen, invSlot);
        input.scroll(1);
        context.waitTick();

        // Another gold should have moved, and the bundle should still be on the cursor
        assertSlotContains(chestSlot, Items.GOLD_INGOT, 6);
        assertSlotContains(invSlot, Items.GOLD_INGOT, 2);
        context.runOnClient(mc -> assertCarried(mc, Items.BUNDLE, 1));

        // === Push with bundle on cursor, target slot has only 1 item (final click must be left, not right) ===
        Slot chestSlot2 = screen.getMenu().slots.get(1);
        Slot invSlot2 = screen.getMenu().slots.get(28);
        world.getServer().runCommand("item replace block 0 ~ 1 container.1 with minecraft:emerald 1");
        context.waitTick();

        setCursorToSlot(context, screen, chestSlot2);
        input.scroll(-1);
        context.waitTick();

        assertSlotEmpty(chestSlot2);
        assertSlotContains(invSlot2, Items.EMERALD, 1);
        context.runOnClient(mc -> assertCarried(mc, Items.BUNDLE, 1));

        // === Pull with bundle on cursor, source slot has only 1 item (final click must be left, not right) ===
        Slot chestSlot3 = screen.getMenu().slots.get(2);
        Slot invSlot3 = screen.getMenu().slots.get(29);
        world.getServer().runCommand("item replace block 0 ~ 1 container.2 with minecraft:iron_ingot 1");
        world.getServer().runCommand("item replace entity @p inventory.2 with minecraft:iron_ingot 1");
        context.waitTick();

        setCursorToSlot(context, screen, invSlot3);
        input.scroll(1);
        context.waitTick();

        assertSlotEmpty(chestSlot3);
        assertSlotContains(invSlot3, Items.IRON_INGOT, 2);
        context.runOnClient(mc -> assertCarried(mc, Items.BUNDLE, 1));

        // Put the bundle back
        setCursorToSlot(context, screen, hotbarSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Close the chest
        context.setScreen(() -> null);
        context.waitTick();
    }

    /**
     * Tests crafting output slot behaviors with the wheel tweak and LMB tweak.
     * Uses a crafting table with oak logs -> oak planks (1 log = 4 planks).
     */
    private void testCraftingOutputSlot(ClientGameTestContext context, TestSingleplayerContext world) {
        // Clear inventory
        world.getServer().runCommand("clear @p");

        // Place a crafting table in front of the player
        world.getServer().runCommand("setblock 0 ~ 1 minecraft:crafting_table");

        // Give player oak logs for crafting planks (1 log = 4 planks)
        world.getServer().runCommand("item replace entity @p hotbar.0 with minecraft:oak_log 10");
        context.waitTick();

        // Make player look at the crafting table
        context.runOnClient(mc -> mc.player.setXRot(45));
        context.waitTick();

        // Open crafting table
        context.getInput().pressKey(options -> options.keyUse);
        context.waitForScreen(AbstractContainerScreen.class);

        AbstractContainerScreen<?> screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
        TestInput input = context.getInput();

        // Slot indices for crafting table:
        // 0: output, 1-9: crafting grid (3x3), 10-36: player inventory, 37-45: hotbar
        Slot outputSlot = screen.getMenu().slots.get(0);
        Slot craftingSlot = screen.getMenu().slots.get(1);
        Slot invSlot = screen.getMenu().slots.get(10);
        Slot hotbarSlot = screen.getMenu().slots.get(37);

        // Place all logs in crafting slot (need 3 for the tests)
        setCursorToSlot(context, screen, hotbarSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
        setCursorToSlot(context, screen, craftingSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Verify setup: crafting has logs, output shows 4 planks
        assertSlotContains(craftingSlot, Items.OAK_LOG, 10);
        assertSlotContains(outputSlot, Items.OAK_PLANKS, 4);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // === Test 1: Scroll from crafting output (push) crafts 1 batch, leaves nothing on mouse ===
        setCursorToSlot(context, screen, outputSlot);
        input.scroll(-1);
        context.waitTick();

        // Crafting consumed 1 log, planks distributed to inventory
        assertSlotContains(craftingSlot, Items.OAK_LOG, 9);
        assertSlotContains(invSlot, Items.OAK_PLANKS, 4);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // Wait for the crafting output to appear again
        context.waitTick();
        assertSlotContains(outputSlot, Items.OAK_PLANKS, 4);

        // === Test 2: Scroll to pull from crafting output into compatible slot ===
        setCursorToSlot(context, screen, invSlot);
        input.scroll(1);
        context.waitTick();

        // Crafting consumed 1 log, planks pulled into invSlot
        assertSlotContains(craftingSlot, Items.OAK_LOG, 8);
        assertSlotContains(invSlot, Items.OAK_PLANKS, 8); // 4 + 4
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // Wait for the crafting output to appear again
        context.waitTick();
        assertSlotContains(outputSlot, Items.OAK_PLANKS, 4);

        // === Test 3: LMB drag with same item over crafting output ===
        // Start from invSlot which has planks
        setCursorToSlot(context, screen, invSlot);
        input.holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Verify we picked up the planks
        context.runOnClient(mc -> assertCarried(mc, Items.OAK_PLANKS, 8));

        // Drag to crafting output
        setCursorToSlot(context, screen, outputSlot);

        // Crafting consumed 1 log, planks added to mouse (8 + 4 = 12)
        assertSlotContains(craftingSlot, Items.OAK_LOG, 7);
        context.runOnClient(mc -> assertCarried(mc, Items.OAK_PLANKS, 12));

        // Wait for the crafting output to appear again
        context.waitTick();
        assertSlotContains(outputSlot, Items.OAK_PLANKS, 4);

        input.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Clean up: put items in an empty slot
        setCursorToSlot(context, screen, invSlot);
        input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();

        // Close the crafting table
        context.setScreen(() -> null);
        context.waitTick();
    }

    /**
     * Tests that scrolling to pull items from a furnace input slot does not reset the smelting progress.
     */
    private void testFurnaceSmeltingPreserved(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("clear @p");

        // Place a furnace and set up smelting: raw iron in input, coal as fuel.
        world.getServer().runCommand("setblock 0 ~ 1 minecraft:furnace");
        context.waitTick();
        world.getServer().runCommand("item replace block 0 ~ 1 container.0 with minecraft:raw_iron 8");
        world.getServer().runCommand("item replace block 0 ~ 1 container.1 with minecraft:coal 8");

        // Open the furnace.
        context.runOnClient(mc -> mc.player.setXRot(45));
        context.waitTick();
        context.getInput().pressKey(options -> options.keyUse);
        context.waitForScreen(AbstractContainerScreen.class);

        // Fast-forward cooking progress to near completion (190 of 200 ticks).
        world.getServer().runCommand("data merge block 0 ~ 1 {cooking_time_spent:190,cooking_total_time:200}");
        context.waitTick();

        AbstractContainerScreen<?> screen = context.computeOnClient(mc -> (AbstractContainerScreen<?>) mc.screen);
        TestInput input = context.getInput();

        // Furnace slots: 0=input, 1=fuel, 2=output.
        // Player inventory: 3-29=main, 30-38=hotbar.
        Slot inputSlot = screen.getMenu().slots.get(0);
        Slot outputSlot = screen.getMenu().slots.get(2);
        Slot playerSlot = screen.getMenu().slots.get(3);

        // Verify no output has been produced yet.
        assertSlotEmpty(outputSlot);

        // Scroll to push 1 item from furnace input to player inventory.
        setCursorToSlot(context, screen, inputSlot);
        input.scroll(-1);
        context.waitTick();

        // Verify items moved correctly: 7 remain in furnace, 1 in player inventory.
        assertSlotContains(inputSlot, Items.RAW_IRON, 7);
        assertSlotContains(playerSlot, Items.RAW_IRON, 1);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

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
        setCursorToSlot(context, screen, playerSlot);
        input.scroll(1);
        context.waitTick();

        // Verify: input has 5 (was 6 after first smelt consumed 1, now -1 more), player has 2.
        assertSlotContains(inputSlot, Items.RAW_IRON, 5);
        assertSlotContains(playerSlot, Items.RAW_IRON, 2);
        context.runOnClient(mc -> assertCarriedEmpty(mc));

        // Wait for smelting to complete.
        context.waitTicks(10);

        // If smelting was preserved, another iron ingot should be in the output.
        assertSlotContains(outputSlot, Items.IRON_INGOT, 2);

        // Close the furnace.
        context.setScreen(() -> null);
        context.waitTick();
    }
}
