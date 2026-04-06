package yalter.mousetweaks.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.Difficulty;
import yalter.mousetweaks.ConfigScreen;

import static yalter.mousetweaks.test.InventoryTestHelper.GUI_SCALE;

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

            var tests = new InventoryTests(context, world);
            tests.testRmbTweak();
            tests.testLmbTweakWithItem();
            tests.testLmbTweakWithoutItem();
            tests.testWheelTweak();
            tests.testWheelTweakPushEdgeCases();
            tests.testWheelTweakPullEdgeCases();
            tests.testWheelTweakWithBundle();
            tests.testCraftingOutputSlot();
            tests.testFurnaceSmeltingPreserved();
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
}
