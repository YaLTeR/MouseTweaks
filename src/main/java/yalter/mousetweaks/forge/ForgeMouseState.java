package yalter.mousetweaks.forge;

import java.util.EnumSet;

import com.google.common.base.MoreObjects;
import org.lwjgl.input.Mouse;
import yalter.mousetweaks.IMouseState;
import yalter.mousetweaks.MouseButton;

/**
 * Mouse state based on LWJGL input events.
 *
 * This mouse state relies on being updated in an LWJGL event handling loop, for example using Forge's mouse input event.
 * Using it with Forge's mouse input event offers good compatibility with other mods (if a click was handled by the GUI
 * it won't be delivered to Mouse Tweaks), but due to Forge only processing events every game tick (rather than every
 * render tick) suffers from the mouse scrolling not being very smooth.
 */
public class ForgeMouseState implements IMouseState {
	private final EnumSet<MouseButton> pressedButtons = EnumSet.noneOf(MouseButton.class);
	private int scrollAmount = 0;

	public ForgeMouseState() {

	}

	/**
	 * Update the current mouse state.
	 */
	@Override
	public void update() {
		MouseButton eventButton = getEventButton();
		if (eventButton != null) {
			if (Mouse.getEventButtonState()) {
				pressedButtons.add(eventButton);
			} else {
				pressedButtons.remove(eventButton);
			}
		} else {
			scrollAmount += Mouse.getEventDWheel();
		}
		// clear any pressed buttons in case we missed them being released
		pressedButtons.removeIf(mouseButton -> !Mouse.isButtonDown(mouseButton.getValue()));
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
	@Override
	public void clear() {
		pressedButtons.clear();
		scrollAmount = 0;
	}

	/**
	 * Returns true if the button was held down during the last mouse update.
	 * Returns false if no button was held down, or the press was started in a different gui.
	 */
	@Override
	public boolean isButtonPressed(MouseButton mouseButton) {
		return pressedButtons.contains(mouseButton);
	}

	/**
	 * Get the scroll amount from the last mouse update.
	 */
	@Override
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
