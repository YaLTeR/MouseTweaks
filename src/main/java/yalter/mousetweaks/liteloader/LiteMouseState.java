package yalter.mousetweaks.liteloader;

import org.lwjgl.input.Mouse;
import yalter.mousetweaks.IMouseState;
import yalter.mousetweaks.MouseButton;

public class LiteMouseState implements IMouseState {
	@Override
	public boolean isButtonPressed(MouseButton mouseButton) {
		return Mouse.isButtonDown(mouseButton.getValue());
	}

	@Override
	public int consumeScrollAmount() {
		return Mouse.getDWheel();
	}
}
