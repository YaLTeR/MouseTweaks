package yalter.mousetweaks;

public enum ScrollItemScaling {
	PROPORTIONAL(0), ALWAYS_ONE(1);

	public static final int scrollStep = 120;

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

	/**
	 * scales the given scroll distance, resulting in the number of items to move, the sign representing the direction
	 */
	public int scale(int scrollDelta) {
		switch (this) {
			case PROPORTIONAL:
				return scrollDelta;
			case ALWAYS_ONE:
				return Integer.signum(scrollDelta) * scrollStep;
			default:
				throw new AssertionError();
		}
	}
}