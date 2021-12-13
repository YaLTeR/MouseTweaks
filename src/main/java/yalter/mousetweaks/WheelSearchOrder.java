package yalter.mousetweaks;

public enum WheelSearchOrder {
    FIRST_TO_LAST(0), LAST_TO_FIRST(1);

    private final int id;

    WheelSearchOrder(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }

    public static WheelSearchOrder fromId(int id) {
        return id == FIRST_TO_LAST.id ? FIRST_TO_LAST : LAST_TO_FIRST;
    }
}
