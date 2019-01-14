package yalter.mousetweaks;

import java.util.EnumSet;

import com.google.common.base.MoreObjects;
import org.lwjgl.input.Mouse;

public class MouseState {
	private final EnumSet<MouseButton> pressedButtons = EnumSet.noneOf(MouseButton.class);
	private int scrollAmount = 0;

	public MouseState() {}

	/**
	 * Update the current mouse state.
	 */
	public void update() {
		MouseButton eventButton = getEventButton();
		if (eventButton != null) {
			if (Mouse.getEventButtonState()) {
				pressedButtons.add(eventButton);
			} else {
				pressedButtons.remove(eventButton);
			}
		} else {
			scrollAmount = Mouse.getEventDWheel();
		}
	}

	private static MouseButton getEventButton() {
		int eventButton = Mouse.getEventButton();
		if (eventButton == 0) {
			return MouseButton.LEFT;
		} else if (eventButton == 1) {
			return MouseButton.RIGHT;
		} else {
			return null;
		}
	}

	/**
	 * Clear the current mouse state, used when changing guis.
	 */
	public void clear() {
		pressedButtons.clear();
		scrollAmount = 0;
	}

	/**
	 * Get the button held down during the last mouse update.
	 * returns null if no button was held down, or the press was started in a different gui
	 */
	public boolean isButtonPressed(MouseButton mouseButton) {
		if (pressedButtons.contains(mouseButton)) {
			boolean confirmPressed = Mouse.isButtonDown(mouseButton.getValue());
			if (!confirmPressed) {
				pressedButtons.remove(mouseButton);
			}
			return confirmPressed;
		}
		return false;
	}

	/**
	 * Get the scroll amount from the last mouse update.
	 */
	public int consumeScrollAmount() {
		int scrollAmount = this.scrollAmount;
		this.scrollAmount = 0;
		return scrollAmount;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("pressedButtons", pressedButtons.toArray())
			.add("scrollAmount", scrollAmount)
			.toString();
	}
}
