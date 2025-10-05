package yalter.mousetweaks;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final Screen previous;

    public ConfigScreen(Screen previous) {
        super(Component.literal("Mouse Tweaks Options"));
        this.previous = previous;
    }

    @Override
    protected void init() {
        Main.config.read();

        this.addRenderableWidget(new StringWidget(this.width / 2 - this.font.width(this.title) / 2, 15, this.width, 9, this.title, this.font));

        this.addRenderableWidget(CycleButton.onOffBuilder(Main.config.rmbTweak)
                .create(this.width / 2 - 155, this.height / 6, 150, 20,
                        Component.literal("RMB Tweak"), (button, value) -> Main.config.rmbTweak = value));

        this.addRenderableWidget(CycleButton.onOffBuilder(Main.config.wheelTweak)
                .create(this.width / 2 - 155, this.height / 6 + 24, 150, 20,
                        Component.literal("Wheel Tweak"), (button, value) -> Main.config.wheelTweak = value));

        this.addRenderableWidget(CycleButton.onOffBuilder(Main.config.lmbTweakWithItem)
                .create(this.width / 2 + 5, this.height / 6, 150, 20,
                        Component.literal("LMB Tweak With Item"), (button, value) -> Main.config.lmbTweakWithItem = value));

        this.addRenderableWidget(CycleButton.onOffBuilder(Main.config.lmbTweakWithoutItem)
                .create(this.width / 2 + 5, this.height / 6 + 24, 150, 20,
                        Component.literal("LMB Tweak Without Item"), (button, value) -> Main.config.lmbTweakWithoutItem = value));

        this.addRenderableWidget(
                CycleButton.builder((WheelSearchOrder value) -> Component.literal(switch (value) {
                            case FIRST_TO_LAST -> "First to Last";
                            case LAST_TO_FIRST -> "Last to First";
                        }))
                        .withValues(WheelSearchOrder.FIRST_TO_LAST, WheelSearchOrder.LAST_TO_FIRST)
                        .withInitialValue(Main.config.wheelSearchOrder)
                        .create(this.width / 2 - 155, this.height / 6 + 24 * 2, 310, 20,
                                Component.literal("Wheel Tweak Search Order"), (button, value) -> Main.config.wheelSearchOrder = value));

        this.addRenderableWidget(
                CycleButton.builder((WheelScrollDirection value) -> Component.literal(switch (value) {
                            case NORMAL -> "Down to Push, Up to Pull";
                            case INVERTED -> "Up to Push, Down to Pull";
                            case INVENTORY_POSITION_AWARE -> "Inventory Position Aware";
                            case INVENTORY_POSITION_AWARE_INVERTED -> "Inventory Position Aware, Inverted";
                        }))
                        .withValues(WheelScrollDirection.NORMAL, WheelScrollDirection.INVERTED, WheelScrollDirection.INVENTORY_POSITION_AWARE, WheelScrollDirection.INVENTORY_POSITION_AWARE_INVERTED)
                        .withInitialValue(Main.config.wheelScrollDirection)
                        .create(this.width / 2 - 155, this.height / 6 + 24 * 3, 310, 20,
                                Component.literal("Scroll Direction"), (button, value) -> Main.config.wheelScrollDirection = value));

        this.addRenderableWidget(
                CycleButton.builder((ScrollItemScaling value) -> Component.literal(switch (value) {
                            case PROPORTIONAL -> "Multiple Wheel Clicks Move Multiple Items";
                            case ALWAYS_ONE -> "Always Move One Item (macOS Compatibility)";
                        }))
                        .withValues(ScrollItemScaling.PROPORTIONAL, ScrollItemScaling.ALWAYS_ONE)
                        .withInitialValue(Main.config.scrollItemScaling)
                        .create(this.width / 2 - 155, this.height / 6 + 24 * 4, 310, 20,
                                Component.literal("Scroll Scaling"), (button, value) -> Main.config.scrollItemScaling = value));

        this.addRenderableWidget(CycleButton.onOffBuilder(Config.debug)
                .create(this.width / 2 - 155, this.height / 6 + 24 * 5, 310, 20,
                        Component.literal("Debug Mode"), (button, value) -> Config.debug = value));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(previous);
    }

    @Override
    public void removed() {
        Main.config.save();
    }
}
