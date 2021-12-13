package yalter.mousetweaks;

public interface IMouseState {
    /**
     * Returns true if the the button is held down.
     */
    boolean isButtonPressed(MouseButton mouseButton);

    /**
     * Get the scroll amount.
     */
    int consumeScrollAmount();

    /**
     * Update the current mouse state (if any).
     */
    default void update() {
    }

    /**
     * Clear the current mouse state (if any), used when changing guis.
     */
    default void clear() {
    }
}
