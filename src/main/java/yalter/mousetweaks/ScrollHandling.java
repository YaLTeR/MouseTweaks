package yalter.mousetweaks;

public enum ScrollHandling {
    SIMPLE(0), EVENT_BASED(1);

    private final int id;

    ScrollHandling(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }

    public static ScrollHandling fromId(int id) {
        if (id == EVENT_BASED.id) {
            return EVENT_BASED;
        } else {
            return SIMPLE;
        }
    }
}