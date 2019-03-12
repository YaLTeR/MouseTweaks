package yalter.mousetweaks;

public enum ScrollItemScaling {
	PROPORTIONAL(0), ALWAYS_ONE(1);

	private final int id;

	ScrollItemScaling(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	public static ScrollItemScaling fromId(int id) {
		if (id == PROPORTIONAL.id) {
			return PROPORTIONAL;
		} else {
			return ALWAYS_ONE;
		}
	}
}