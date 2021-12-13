package yalter.mousetweaks;

public enum WheelScrollDirection {
    NORMAL(0), INVERTED(1), INVENTORY_POSITION_AWARE(2), INVENTORY_POSITION_AWARE_INVERTED(3);

    private final int id;

    WheelScrollDirection(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }

    public static WheelScrollDirection fromId(int id) {
        if (id == NORMAL.id) {
            return NORMAL;
        } else if (id == INVERTED.id) {
            return INVERTED;
        } else if (id == INVENTORY_POSITION_AWARE.id) {
            return INVENTORY_POSITION_AWARE;
        } else {
            return INVENTORY_POSITION_AWARE_INVERTED;
        }
    }

    public boolean isInverted() {
        return this == INVERTED || this == INVENTORY_POSITION_AWARE_INVERTED;
    }

    public boolean isPositionAware() {
        return this == INVENTORY_POSITION_AWARE || this == INVENTORY_POSITION_AWARE_INVERTED;
    }
}
