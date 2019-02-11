package yalter.mousetweaks;

public enum MouseHandling {
	SIMPLE(0), EVENT_BASED(1);

	private final int id;

	MouseHandling(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	public static MouseHandling fromId(int id) {
		if (id == EVENT_BASED.id) {
			return EVENT_BASED;
		} else {
			return SIMPLE;
		}
	}
}