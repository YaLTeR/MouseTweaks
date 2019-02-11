package yalter.mousetweaks;

import org.lwjgl.input.Mouse;

public class SimpleMouseState implements IMouseState {
	@Override
	public boolean isButtonPressed(MouseButton mouseButton) {
		return Mouse.isButtonDown(mouseButton.getValue());
	}

	@Override
	public int consumeScrollAmount() {
		return Mouse.getDWheel();
	}
}
