package yalter.mousetweaks;

public class Logger {

	public static void Log(String text) {
		System.out.println("[Mouse Tweaks] " + text);
	}

	public static void DebugLog(String text) {
		if (Main.Debug != 0)
			System.out.println("[Mouse Tweaks] " + text);
	}

}
