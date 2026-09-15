package yalter.mousetweaks;

import com.mojang.blaze3d.platform.InputConstants;

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
            case InputConstants.MOUSE_BUTTON_LEFT -> MouseButton.LEFT;
            case InputConstants.MOUSE_BUTTON_RIGHT -> MouseButton.RIGHT;
            default -> null;
        };
    }
}
