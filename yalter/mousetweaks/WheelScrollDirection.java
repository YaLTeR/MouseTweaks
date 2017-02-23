package yalter.mousetweaks;

public enum WheelScrollDirection {
	NORMAL(0),
	INVERTED(1);

	private final int id;
	WheelScrollDirection(int id) {
		this.id = id;
	}
	public int getValue() {
		return id;
	}

	public static WheelScrollDirection fromId(int id) {
		return id == NORMAL.id ? NORMAL : INVERTED;
	}
}
