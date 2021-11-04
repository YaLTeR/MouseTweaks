package yalter.mousetweaks;

import org.lwjgl.glfw.GLFW;

public enum MouseButton {
	LEFT(0), RIGHT(1);

	private final int id;

	MouseButton(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	public static MouseButton fromEventButton(int eventButton) {
		return switch (eventButton) {
			case GLFW.GLFW_MOUSE_BUTTON_LEFT -> MouseButton.LEFT;
			case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> MouseButton.RIGHT;
			default -> null;
		};
	}
}
