package yalter.mousetweaks;

import org.lwjgl.input.Mouse;

/**
 * Simple polling mouse state.
 * <p>
 * This has an advantage of offering smooth scrolling (if polled every render tick it will return scroll events
 * appropriately). Unfortunately, it doesn't always play well with other mods: clicks handled by other mods will also be
 * reported which sometimes leads to issues, see:
 * - https://github.com/YaLTeR/MouseTweaks/issues/2
 * - https://github.com/YaLTeR/MouseTweaks/issues/17
 * - https://github.com/YaLTeR/MouseTweaks/issues/19
 */
public class SimpleMouseState implements IMouseState {
	private double scrollAmount = 0;

	@Override
	public boolean isButtonPressed(MouseButton mouseButton) {
		return Mouse.isButtonDown(mouseButton.getValue());
	}

	@Override
	public void clear() {
		scrollAmount = 0;
	}

	@Override
	public int consumeScrollAmount() {
		scrollAmount += Main.config.scrollItemScaling.scale(Mouse.getDWheel());

		int amountToConsume = (int) scrollAmount;
		scrollAmount -= amountToConsume;
		return amountToConsume;
	}
}
